package com.example.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.data.local.ChatMessageEntity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") ShenError else ShenEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "®️SHΞN™Hᴇʀᴏ",
                                color = ShenCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                systemStatus,
                                color = ShenTextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showToolsSheet = true }) {
                        Icon(Icons.Default.Extension, contentDescription = "Agentic Tools", tint = ShenCyan)
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings & API Keys", tint = ShenCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShenSurfaceDark)
            )
        },
        containerColor = ShenBgDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Aether Living Background Shader
            AetherShaderBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section: Central Holographic Avatar Core & Status Banner
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Central Living Holographic Avatar Core (Always active on mobile screen)
                    Box(
                        modifier = Modifier
                            .size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening) {
                            ListeningIndicator(size = 110.dp, color = ShenEmerald)
                        }

                        val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 0.95f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )

                        Box(
                            modifier = Modifier
                                .size((85 * pulseScale).dp)
                                .clip(RoundedCornerShape(42.dp))
                                .background(
                                    Brush.radialGradient(
                                        listOf(ShenCardDark, ShenSurfaceDark, ShenBgDark)
                                    )
                                )
                                .border(2.dp, if (isListening) ShenEmerald else ShenCyan, RoundedCornerShape(42.dp))
                                .clickable {
                                    viewModel.toggleVoiceListening()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.SmartToy,
                                    contentDescription = "SHEN Avatar",
                                    tint = if (isListening) ShenEmerald else ShenCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    if (isListening) "LISTENING" else "SHEN AI",
                                    color = if (isListening) ShenEmerald else ShenCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Middle Section: Unified Chat & Grounded Results Feed
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
                                "®️SHΞN™Hᴇʀᴏ Neural Core Online",
                                color = ShenCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Ask anything, search the web in real-time, upload files, or speak your command. SHEN handles everything seamlessly.",
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
                            items(messages) { msg ->
                                val isUser = msg.sender == "user"
                                val isError = msg.mode == "error"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 320.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isUser) ShenCardDark else if (isError) ShenError.copy(alpha = 0.2f) else ShenSurfaceDark)
                                            .border(1.dp, if (isUser) ShenCyan.copy(alpha = 0.4f) else if (isError) ShenError else ShenEmerald.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                                                    contentDescription = null,
                                                    tint = if (isUser) ShenCyan else ShenEmerald,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isUser) "OPERATOR" else "SHEN HERO",
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
                        trackColor = ShenCardDark
                    )
                }

                // Bottom Section: Universal Natural Language Command Bar & File Uploader
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = ShenSurfaceDark),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, ShenCyan.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attachment / File Upload Button
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Upload File", tint = ShenCyan)
                        }

                        // Voice Listening Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleVoiceListening() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = "Voice Input",
                                tint = if (isListening) ShenEmerald else ShenTextSecondary
                            )
                        }

                        // Natural Language Text Input
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Ask SHEN, search web, or give command...", color = ShenTextSecondary, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = ShenTextPrimary,
                                unfocusedTextColor = ShenTextPrimary,
                                cursorColor = ShenCyan
                            ),
                            maxLines = 2
                        )

                        // Send Button with Intent Parsing
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(ShenCyan)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = ShenBgDark)
                        }
                    }
                }
            }
        }
    }

    // Settings Modal Sheet
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = ShenBgDark
        ) {
            SettingsScreen(viewModel)
        }
    }

    // Tools Modal Sheet
    if (showToolsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showToolsSheet = false },
            containerColor = ShenBgDark
        ) {
            ToolsSheetContent(viewModel, onClose = { showToolsSheet = false })
        }
    }
}

@Composable
fun AetherShaderBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "aetherShader")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val minDim = minOf(width, height)

        drawRect(color = ShenBgDark)

        for (i in 0..4) {
            val alpha = (0.1f + 0.04f * i)
            drawCircle(
                color = if (i % 2 == 0) ShenCyan.copy(alpha = alpha) else ShenEmerald.copy(alpha = alpha),
                radius = minDim * (0.25f + i * 0.15f),
                center = androidx.compose.ui.geometry.Offset(width / 2f, height / 2f),
                style = Stroke(width = 1.5.dp.toPx())
            )
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
        Text("AGENTIC TOOLKIT & GROUNDING", color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = ShenCardDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ShenCyan.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = ShenTextSecondary, fontSize = 12.sp)
        }
    }
}
