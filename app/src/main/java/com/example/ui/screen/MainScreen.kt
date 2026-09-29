package com.example.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.GeminiGlowCard
import com.example.ui.components.ListeningIndicator
import com.example.ui.theme.ShenCyan
import com.example.ui.theme.ShenEmerald
import com.example.ui.theme.ShenError
import com.example.ui.theme.ShenTextPrimary
import com.example.ui.theme.ShenTextSecondary
import com.example.ui.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AssistantViewModel) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val apiKey by viewModel.currentApiKey.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var voiceMessage by remember { mutableStateOf<String?>(null) }

    val speechRecognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    val recognitionListener = remember(speechRecognizer) {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = viewModel.setListening(true)
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = viewModel.setListening(false)
            override fun onError(error: Int) {
                viewModel.setListening(false)
                voiceMessage = "Voice input stopped. Tap the microphone and try again."
            }
            override fun onResults(results: Bundle?) {
                viewModel.setListening(false)
                val recognized = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!recognized.isNullOrBlank()) {
                    textInput = listOf(textInput, recognized).filter { it.isNotBlank() }.joinToString(" ")
                    voiceMessage = null
                } else {
                    voiceMessage = "No speech was recognized. Try again."
                }
            }
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }
    }
    DisposableEffect(speechRecognizer, recognitionListener) {
        speechRecognizer?.setRecognitionListener(recognitionListener)
        onDispose {
            speechRecognizer?.destroy()
            viewModel.setListening(false)
        }
    }

    val startVoiceRecognition: () -> Unit = {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            voiceMessage = "Speech recognition is not available on this device."
        } else {
            voiceMessage = null
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                }
                viewModel.setListening(true)
                recognizer.startListening(intent)
            } catch (_: Exception) {
                viewModel.setListening(false)
                voiceMessage = "Voice input could not start. Try again."
            }
        }
    }
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceRecognition() else voiceMessage = "Allow microphone access to use voice input."
    }
    val requestVoiceInput: () -> Unit = {
        if (viewModel.isListening.value) {
            speechRecognizer?.stopListening()
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecognition()
        } else {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.sendMessage("Please analyze this image.", imageUri = uri)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (apiKey.isBlank()) ShenError else ShenEmerald)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("®️SHΞN™Hᴇʀᴏ", color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ShenCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000000))
            )
        },
        containerColor = Color(0xFF000000)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (isListening) ListeningIndicator(size = 124.dp, color = ShenEmerald)
                AvatarView(size = 112.dp, isThinking = isLoading)
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Neural core ready", color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Chat with Gemini, search the web, attach an image, or dictate a message.",
                            color = ShenTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages, key = { it.id }) { message -> MessageBubble(message) }
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ShenCyan)
            }
            voiceMessage?.let { Text(it, color = ShenTextSecondary, fontSize = 11.sp) }

            GeminiGlowCard(
                shape = RoundedCornerShape(22.dp),
                containerColor = Color(0xFF0A0A0A),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        enabled = !isLoading,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach image", tint = ShenCyan)
                    }
                    IconButton(onClick = requestVoiceInput, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = if (isListening) "Stop voice input" else "Dictate message",
                            tint = if (isListening) ShenEmerald else ShenTextSecondary
                        )
                    }
                    IconButton(onClick = { searchMode = !searchMode }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = if (searchMode) "Turn off web search" else "Search the web",
                            tint = if (searchMode) ShenEmerald else ShenTextSecondary
                        )
                    }
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Message Gemini...", color = ShenTextSecondary, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = ShenTextPrimary,
                            unfocusedTextColor = ShenTextPrimary,
                            cursorColor = ShenCyan
                        ),
                        maxLines = 3
                    )
                    IconButton(
                        onClick = {
                            val prompt = textInput.trim()
                            if (prompt.isNotEmpty()) {
                                viewModel.sendMessage(prompt, if (searchMode) "search" else "chat")
                                textInput = ""
                                voiceMessage = null
                            }
                        },
                        enabled = textInput.isNotBlank() && !isLoading,
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(19.dp)).background(ShenCyan)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send message", tint = Color.Black)
                    }
                }
            }
        }
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = Color(0xFF000000)
        ) {
            SettingsScreen(viewModel)
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessageEntity) {
    val isUser = message.sender == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        GeminiGlowCard(
            shape = RoundedCornerShape(14.dp),
            containerColor = Color(0xFF0F0F0F),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = if (isUser) ShenCyan else ShenEmerald,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (isUser) "YOU" else if (message.mode == "error") "NOTICE" else "SHEN",
                        color = if (isUser) ShenCyan else ShenEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (message.mode == "search") {
                        Spacer(Modifier.width(6.dp))
                        Text("WEB SEARCH", color = ShenCyan, fontSize = 9.sp)
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(message.text, color = ShenTextPrimary, fontSize = 13.sp)
            }
        }
    }
}
