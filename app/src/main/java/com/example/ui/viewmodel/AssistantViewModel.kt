package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Content
import com.example.data.GenerateContentRequest
import com.example.data.GeminiApiClient
import com.example.data.InlineData
import com.example.data.Part
import com.example.data.SecurePreferences
import com.example.data.buildGeminiConversation
import com.example.data.local.AssistantDatabase
import com.example.data.local.ChatMessageEntity
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import retrofit2.HttpException

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val chatDao = AssistantDatabase.getDatabase(application).chatDao()
    private val securePreferences = SecurePreferences(application)
    private val requestMutex = Mutex()

    val messages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .map { saved -> saved.map { it.copy(text = securePreferences.decryptValue(it.text)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _currentApiKey = MutableStateFlow(securePreferences.getString(GEMINI_KEY))
    val currentApiKey: StateFlow<String> = _currentApiKey.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _systemStatus = MutableStateFlow("READY · GEMINI ASSISTANT")
    val systemStatus: StateFlow<String> = _systemStatus.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.getAllMessagesOnce().forEach { message ->
                if (!message.text.startsWith(ENCRYPTED_PREFIX)) {
                    chatDao.updateMessage(message.copy(text = securePreferences.encryptValue(message.text)))
                }
            }
        }
    }

    fun updateApiKey(key: String) {
        securePreferences.putString(GEMINI_KEY, key.trim())
        _currentApiKey.value = key.trim()
    }

    fun setListening(listening: Boolean) {
        _isListening.value = listening
        _systemStatus.value = if (listening) "LISTENING" else "READY · GEMINI ASSISTANT"
    }

    fun sendMessage(userPrompt: String, mode: String = "chat", imageUri: Uri? = null) {
        if (userPrompt.isBlank() || !requestMutex.tryLock()) return

        viewModelScope.launch {
            _isLoading.value = true
            _systemStatus.value = "CONNECTING TO GEMINI"
            try {
                val recentMessages = chatDao.getRecentMessages(MAX_CONTEXT_MESSAGES)
                    .asReversed()
                    .map { it.copy(text = securePreferences.decryptValue(it.text)) }
                val imagePart = imageUri?.let { withContext(Dispatchers.IO) { readImagePart(it) } }
                val visiblePrompt = if (imagePart == null) userPrompt else "$userPrompt\n[Image attached]"
                saveMessage(ChatMessageEntity(sender = "user", text = visiblePrompt, mode = mode))

                val apiKey = _currentApiKey.value
                if (apiKey.isBlank()) {
                    saveMessage(
                        ChatMessageEntity(
                            sender = "shen",
                            text = "Add your Gemini API key in Settings before sending a message.",
                            mode = "error"
                        )
                    )
                    return@launch
                }

                val request = GenerateContentRequest(
                    contents = buildGeminiConversation(recentMessages, userPrompt, imagePart),
                    systemInstruction = Content(
                        parts = listOf(
                            Part(
                                text = "You are SHEN Hero, a helpful assistant. Be clear and accurate. " +
                                    "For current information, use Google Search grounding when it is enabled."
                            )
                        )
                    ),
                    tools = if (mode == "search") listOf(JsonObject(mapOf("googleSearch" to JsonObject(emptyMap())))) else null
                )

                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val replyText = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.mapNotNull { it.text }
                    ?.joinToString("\n")
                    ?.takeIf { it.isNotBlank() }
                    ?: "Gemini returned no text. Please try again."
                saveMessage(ChatMessageEntity(sender = "shen", text = replyText, mode = mode))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val userMessage = when (error) {
                    is HttpException -> when (error.code()) {
                        400 -> "Gemini rejected the request. Check the selected model and prompt, then try again."
                        403 -> "Gemini rejected this API key or the project does not have access to this model."
                        404 -> "The configured Gemini model was not found. Update the model endpoint and try again."
                        429 -> "Gemini rate limit or quota reached. Wait a while or review billing and usage limits."
                        else -> "Gemini is temporarily unavailable (${error.code()}). Try again shortly."
                    }
                    is IllegalArgumentException -> error.message ?: "The selected image could not be processed."
                    else -> "I couldn't reach Gemini. Check your internet connection and try again."
                }
                saveMessage(
                    ChatMessageEntity(
                        sender = "shen",
                        text = userMessage,
                        mode = "error"
                    )
                )
            } finally {
                _isLoading.value = false
                _systemStatus.value = "READY · GEMINI ASSISTANT"
                requestMutex.unlock()
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch { chatDao.clearHistory() }
    }

    private suspend fun saveMessage(message: ChatMessageEntity) {
        chatDao.insertMessage(message.copy(text = securePreferences.encryptValue(message.text)))
    }

    private suspend fun readImagePart(uri: Uri): Part = withContext(Dispatchers.IO) {
        val resolver = getApplication<Application>().contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "The selected image could not be read." }

        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_IMAGE_SIDE) sampleSize *= 2
        val bitmap = resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(
                input,
                null,
                BitmapFactory.Options().apply { inSampleSize = sampleSize }
            )
        } ?: throw IllegalArgumentException("The selected image could not be opened.")

        val encoded = ByteArrayOutputStream().use { output ->
            bitmap.useAndRecycle { compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output) }
            output.toByteArray()
        }
        require(encoded.size <= MAX_IMAGE_BYTES) { "This image is too large to send. Choose a smaller image." }
        Part(inlineData = InlineData(mimeType = "image/jpeg", data = Base64.encodeToString(encoded, Base64.NO_WRAP)))
    }

    private inline fun <T> Bitmap.useAndRecycle(block: Bitmap.() -> T): T = try {
        block()
    } finally {
        recycle()
    }

    private companion object {
        const val GEMINI_KEY = "gemini_api_key"
        const val MAX_CONTEXT_MESSAGES = 20
        const val MAX_IMAGE_SIDE = 1_536
        const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
        const val JPEG_QUALITY = 85
        const val ENCRYPTED_PREFIX = "enc:v1:"
    }
}
