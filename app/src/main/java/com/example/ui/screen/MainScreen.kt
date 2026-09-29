package com.example.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.data.local.ChatMessageEntity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarView
import com.example.ui.components.GeminiGlowCard
import com.example.ui.components.ListeningIndicator
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AssistantViewModel) {
    val systemStatus by viewModel.systemStatus.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val apiKey by viewModel.currentApiKey.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showToolsSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.sendMessage("📎 [Attached File/Image]: $uri. Please analyze this file.", "chat")
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
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
                                .background(if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") ShenError else ShenEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "®️SHΞN™Hᴇʀᴏ",
                            color = ShenCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showToolsSheet = true }) {
                        Icon(Icons.Default.Extension, contentDescription = "Tools", tint = ShenCyan)
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ShenCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000000))
            )
        },
        containerColor = Color(0xFF000000)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Central Avatar Section (Pure Neomorphic & Native Compose AvatarView)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clickable { viewModel.toggleVoiceListening() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening) {
                            ListeningIndicator(size = 130.dp, color = ShenEmerald)
                        }

                        AvatarView(
                            size = 120.dp,
                            isThinking = isLoading,
                            isSleeping = false
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Chat & Grounded Results Feed
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Neural Core Initialized",
                                color = ShenCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Ask anything, search the web in real-time, or tap the avatar for voice mode.",
                                color = ShenTextSecondary,
                                fontSize = 12.sp,
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
                            items(messages) { msg ->
                                val isUser = msg.sender == "user"
                                val isError = msg.mode == "error"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    GeminiGlowCard(
                                        shape = RoundedCornerShape(14.dp),
                                        containerColor = Color(0xFF0F0F0F),
                                        modifier = Modifier.widthIn(max = 320.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                                                    contentDescription = null,
                                                    tint = if (isUser) ShenCyan else ShenEmerald,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isUser) "OPERATOR" else "SHEN",
                                                    color = if (isUser) ShenCyan else ShenEmerald,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (msg.mode == "search") {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("🌐 Grounded", color = ShenCyan, fontSize = 9.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = msg.text,
                                                color = ShenTextPrimary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = ShenCyan,
                        trackColor = Color(0xFF111111)
                    )
                }

                // Bottom Command Bar with Gemini Wavy RGB Glow Border
                GeminiGlowCard(
                    shape = RoundedCornerShape(22.dp),
                    containerColor = Color(0xFF0A0A0A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = ShenCyan)
                        }

                        IconButton(
                            onClick = { viewModel.toggleVoiceListening() },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = "Voice",
                                tint = if (isListening) ShenEmerald else ShenTextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Type command or search...", color = ShenTextSecondary, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = ShenTextPrimary,
                                unfocusedTextColor = ShenTextPrimary,
                                cursorColor = ShenCyan
                            ),
                            maxLines = 2
                        )

                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    val prompt = textInput
                                    textInput = ""
                                    val lower = prompt.lowercase()
                                    val mode = if (lower.contains("search") || lower.contains("google") || lower.contains("news") || lower.contains("latest") || lower.contains("چه خبر") || lower.contains("امروز")) {
                                        "search"
                                    } else {
                                        "chat"
                                    }
                                    viewModel.sendMessage(prompt, mode)
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(ShenCyan)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF000000))
                        }
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

    if (showToolsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showToolsSheet = false },
            containerColor = Color(0xFF000000)
        ) {
            ToolsSheetContent(viewModel, onClose = { showToolsSheet = false })
        }
    }
}

@Composable
fun ToolsSheetContent(viewModel: AssistantViewModel, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text("AGENTIC TOOLKIT & GROUNDING", color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(16.dp))

        ToolItem(
            title = "🌐 Real-Time Web Search Grounding",
            description = "Query Google Search for latest news and live facts.",
            onClick = {
                onClose()
                viewModel.sendMessage("Search Google for latest breakthrough in artificial intelligence today.", "search")
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ToolItem(
            title = "📍 Geospatial Maps Intelligence",
            description = "Analyze location data and coordinates.",
            onClick = {
                onClose()
                viewModel.sendMessage("Provide geospatial analysis for San Francisco.", "chat")
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ToolItem(
            title = "⚙️ Neural System Diagnostics",
            description = "Run full system diagnostic check.",
            onClick = {
                onClose()
                viewModel.sendMessage("Run full system diagnostic report.", "chat")
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ToolItem(title: String, description: String, onClick: () -> Unit) {
    GeminiGlowCard(
        shape = RoundedCornerShape(12.dp),
        containerColor = Color(0xFF0F0F0F),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = ShenTextSecondary, fontSize = 11.sp)
        }
    }
}
