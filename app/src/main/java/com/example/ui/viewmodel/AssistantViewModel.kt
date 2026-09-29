package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AssistantDatabase
import com.example.data.Content
import com.example.data.GenerateContentRequest
import com.example.data.GeminiApiClient
import com.example.data.Part
import com.example.data.local.ChatMessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val chatDao = AssistantDatabase.getDatabase(application).chatDao()

    val messages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val prefs = application.getSharedPreferences("shen_prefs", android.content.Context.MODE_PRIVATE)

    private val _currentApiKey = MutableStateFlow(prefs.getString("gemini_api_key", BuildConfig.GEMINI_API_KEY) ?: BuildConfig.GEMINI_API_KEY)
    val currentApiKey: StateFlow<String> = _currentApiKey.asStateFlow()

    private val _githubPat = MutableStateFlow(prefs.getString("github_pat", "") ?: "")
    val githubPat: StateFlow<String> = _githubPat.asStateFlow()

    private val _cloudflareToken = MutableStateFlow(prefs.getString("cloudflare_token", "") ?: "")
    val cloudflareToken: StateFlow<String> = _cloudflareToken.asStateFlow()

    private val _serperKey = MutableStateFlow(prefs.getString("serper_key", "") ?: "")
    val serperKey: StateFlow<String> = _serperKey.asStateFlow()

    private val _deepseekKey = MutableStateFlow(prefs.getString("deepseek_key", "") ?: "")
    val deepseekKey: StateFlow<String> = _deepseekKey.asStateFlow()

    private val _openaiKey = MutableStateFlow(prefs.getString("openai_key", "") ?: "")
    val openaiKey: StateFlow<String> = _openaiKey.asStateFlow()

    private val _openrouterKey = MutableStateFlow(prefs.getString("openrouter_key", "") ?: "")
    val openrouterKey: StateFlow<String> = _openrouterKey.asStateFlow()

    private val _groqKey = MutableStateFlow(prefs.getString("groq_key", "") ?: "")
    val groqKey: StateFlow<String> = _groqKey.asStateFlow()

    private val _telegramToken = MutableStateFlow(prefs.getString("telegram_token", "") ?: "")
    val telegramToken: StateFlow<String> = _telegramToken.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: HUD, 1: Chat, 2: Voice, 3: Tools, 4: Settings
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _systemStatus = MutableStateFlow("ONLINE · NEURAL LIVE LINK ACTIVE")
    val systemStatus: StateFlow<String> = _systemStatus.asStateFlow()

    fun updateApiKey(key: String) {
        _currentApiKey.value = key
        prefs.edit().putString("gemini_api_key", key).apply()
    }

    fun updateIntegrationKey(type: String, value: String) {
        val editor = prefs.edit()
        when (type) {
            "github" -> { _githubPat.value = value; editor.putString("github_pat", value) }
            "cloudflare" -> { _cloudflareToken.value = value; editor.putString("cloudflare_token", value) }
            "serper" -> { _serperKey.value = value; editor.putString("serper_key", value) }
            "deepseek" -> { _deepseekKey.value = value; editor.putString("deepseek_key", value) }
            "openai" -> { _openaiKey.value = value; editor.putString("openai_key", value) }
            "openrouter" -> { _openrouterKey.value = value; editor.putString("openrouter_key", value) }
            "groq" -> { _groqKey.value = value; editor.putString("groq_key", value) }
            "telegram" -> { _telegramToken.value = value; editor.putString("telegram_token", value) }
        }
        editor.apply()
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleVoiceListening() {
        _isListening.value = !_isListening.value
        if (_isListening.value) {
            _systemStatus.value = "LISTENING FOR LIVE VOICE STREAM..."
        } else {
            _systemStatus.value = "ONLINE · NEURAL LIVE LINK ACTIVE"
        }
    }

    fun sendMessage(userPrompt: String, mode: String = "chat") {
        if (userPrompt.isBlank()) return
        val apiKey = _currentApiKey.value
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            viewModelScope.launch {
                chatDao.insertMessage(ChatMessageEntity(sender = "user", text = userPrompt, mode = mode))
                chatDao.insertMessage(ChatMessageEntity(sender = "shen", text = "⚠️ ERROR: Gemini API key is not configured. Please enter your valid API key in the Settings screen.", mode = "error"))
            }
            return
        }

        viewModelScope.launch {
            chatDao.insertMessage(ChatMessageEntity(sender = "user", text = userPrompt, mode = mode))
            _isLoading.value = true
            _systemStatus.value = "ANALYZING LIVE NEURAL QUERY..."

            try {
                val systemPrompt = "You are SHEN Hero (®️SHΞN™Hᴇʀᴏ), an ultra-advanced JARVIS-style sci-fi AI assistant supporting live search, maps, multi-provider integrations (GitHub, Cloudflare, Serper, DeepSeek, OpenAI, OpenRouter, Groq, Telegram), code analysis, statistics, and multi-format research output. Respond with precision, futuristic flair, and expert intelligence."
                
                // Add Google Search grounding tool if mode is search
                val toolsList = if (mode == "search") {
                    listOf(JsonObject(mapOf("googleSearch" to JsonObject(emptyMap()))))
                } else null

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    tools = toolsList
                )

                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "Live analysis complete. No direct response generated."

                chatDao.insertMessage(ChatMessageEntity(sender = "shen", text = replyText, mode = mode))
            } catch (e: Exception) {
                chatDao.insertMessage(ChatMessageEntity(sender = "shen", text = "Neural connection error: ${e.localizedMessage ?: "Unknown error"}", mode = "error"))
            } finally {
                _isLoading.value = false
                _systemStatus.value = "ONLINE · NEURAL LIVE LINK ACTIVE"
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.clearHistory()
        }
    }
}
