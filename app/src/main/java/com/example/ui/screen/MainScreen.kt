package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AssistantViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val apiKey by viewModel.currentApiKey.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var tempKeyInput by remember { mutableStateOf(apiKey) }

    val listState = rememberLazyListState()

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
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") ShenError else ShenEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "®️SHΞN™Hᴇʀᴏ",
                                color = ShenCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                systemStatus,
                                color = ShenTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        tempKeyInput = apiKey
                        showSettingsDialog = true 
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ShenCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShenSurfaceDark)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = ShenSurfaceDark) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "HUD") },
                    label = { Text("HUD", fontSize = 11.sp) },
                    selected = selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ShenCyan,
                        selectedTextColor = ShenCyan,
                        unselectedIconColor = ShenTextSecondary,
                        unselectedTextColor = ShenTextSecondary,
                        indicatorColor = ShenCardDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Neural Chat") },
                    label = { Text("Chat", fontSize = 11.sp) },
                    selected = selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ShenCyan,
                        selectedTextColor = ShenCyan,
                        unselectedIconColor = ShenTextSecondary,
                        unselectedTextColor = ShenTextSecondary,
                        indicatorColor = ShenCardDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Voice") },
                    label = { Text("Voice", fontSize = 11.sp) },
                    selected = selectedTab == 2,
                    onClick = { viewModel.setTab(2) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ShenCyan,
                        selectedTextColor = ShenCyan,
                        unselectedIconColor = ShenTextSecondary,
                        unselectedTextColor = ShenTextSecondary,
                        indicatorColor = ShenCardDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Extension, contentDescription = "Tools") },
                    label = { Text("Tools", fontSize = 11.sp) },
                    selected = selectedTab == 3,
                    onClick = { viewModel.setTab(3) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ShenCyan,
                        selectedTextColor = ShenCyan,
                        unselectedIconColor = ShenTextSecondary,
                        unselectedTextColor = ShenTextSecondary,
                        indicatorColor = ShenCardDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp) },
                    selected = selectedTab == 4,
                    onClick = { viewModel.setTab(4) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ShenCyan,
                        selectedTextColor = ShenCyan,
                        unselectedIconColor = ShenTextSecondary,
                        unselectedTextColor = ShenTextSecondary,
                        indicatorColor = ShenCardDark
                    )
                )
            }
        },
        containerColor = ShenBgDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HudDashboardTab(viewModel)
                1 -> ChatTab(viewModel, messages, listState, textInput, onTextChanged = { textInput = it }, isLoading)
                2 -> VoiceTab(viewModel, isListening)
                3 -> ToolsTab(viewModel)
                4 -> SettingsScreen(viewModel)
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("SHEN Neural Configuration", color = ShenCyan) },
            text = {
                Column {
                    Text("Enter your Gemini API Key to activate SHEN Hero neural capabilities:", color = ShenTextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempKeyInput,
                        onValueChange = { tempKeyInput = it },
                        label = { Text("GEMINI_API_KEY") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShenCyan,
                            unfocusedBorderColor = ShenTextSecondary,
                            focusedLabelColor = ShenCyan,
                            cursorColor = ShenCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Note: The key is securely stored in local memory and used for direct Gemini API calls.", color = ShenTextSecondary, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateApiKey(tempKeyInput)
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShenCyan)
                ) {
                    Text("Save & Connect", color = ShenBgDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Cancel", color = ShenTextSecondary)
                }
            },
            containerColor = ShenCardDark
        )
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

        // Draw dark background base
        drawRect(color = ShenBgDark)

        // Render Aether Aurora sine wave lines mirroring the WebGL fragment shader
        for (i in 0..5) {
            val progress = (time * 0.5f + i * 40f) % width
            val alpha = (0.15f + 0.05f * i)
            drawCircle(
                color = if (i % 2 == 0) ShenCyan.copy(alpha = alpha) else ShenEmerald.copy(alpha = alpha),
                radius = minDim * (0.2f + i * 0.12f),
                center = androidx.compose.ui.geometry.Offset(width / 2f + kotlin.math.sin(time * 0.1f + i) * 50f, height / 2f + kotlin.math.cos(time * 0.15f + i) * 50f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun HudDashboardTab(viewModel: AssistantViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Aether Living Shader Background
        AetherShaderBackground()

        // Dark gradient overlay for readability (matching AetherHero overlay)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xBB000000),
                            Color(0x66000000),
                            Color(0x33000000)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Hero Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "®️SHΞN™Hᴇʀᴏ",
                    color = ShenCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "JARVIS Neural Assistant Active. Living Aether Core Online.",
                    color = ShenTextPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Central Living Holographic Avatar Core
            Box(
                modifier = Modifier
                    .size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.92f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                // Outer Ring
                Box(
                    modifier = Modifier
                        .size((170 * pulseScale).dp)
                        .clip(RoundedCornerShape(85.dp))
                        .border(2.dp, ShenCyan.copy(alpha = 0.6f), RoundedCornerShape(85.dp))
                )

                // Inner Core Avatar Button / Display
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(65.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(ShenCardDark, ShenSurfaceDark, ShenBgDark)
                            )
                        )
                        .border(3.dp, ShenEmerald, RoundedCornerShape(65.dp))
                        .clickable {
                            viewModel.setTab(1)
                            viewModel.sendMessage("SHEN Hero online. How may I assist you today, Operator?", "chat")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "SHEN Avatar",
                            tint = ShenCyan,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "SHEN AI",
                            color = ShenEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            "READY",
                            color = ShenTextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Quick Actions & CTAs (matching APEX-UI AetherHero buttons)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.setTab(1)
                            viewModel.sendMessage("Initialize full neural diagnostic scan.", "chat")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShenCyan.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ShenCyan)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = "Initialize", tint = ShenCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Initialize", color = ShenCyan, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.setTab(2) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShenEmerald.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ShenEmerald)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice", tint = ShenEmerald)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice Mode", color = ShenEmerald, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChatTab(
    viewModel: AssistantViewModel,
    messages: List<ChatMessageEntity>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    textInput: String,
    onTextChanged: (String) -> Unit,
    isLoading: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyList(
            listState = listState,
            messages = messages,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = ShenCyan,
                trackColor = ShenCardDark
            )
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ShenSurfaceDark)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChanged,
                placeholder = { Text("Command SHEN Hero...", color = ShenTextSecondary) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShenCyan,
                    unfocusedBorderColor = ShenTextSecondary,
                    focusedTextColor = ShenTextPrimary,
                    unfocusedTextColor = ShenTextPrimary,
                    cursorColor = ShenCyan
                ),
                maxLines = 3
            )
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendMessage(textInput)
                        onTextChanged("")
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ShenCyan)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = ShenBgDark)
            }
        }
    }
}

@Composable
fun LazyList(listState: androidx.compose.foundation.lazy.LazyListState, messages: List<ChatMessageEntity>, modifier: Modifier = Modifier) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp),
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
                        .widthIn(max = 300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isUser) ShenCardDark else if (isError) ShenError.copy(alpha = 0.2f) else ShenSurfaceDark)
                        .border(1.dp, if (isUser) ShenCyan.copy(alpha = 0.5f) else if (isError) ShenError else ShenEmerald.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (isUser) "OPERATOR" else "SHEN HERO",
                            color = if (isUser) ShenCyan else ShenEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.text,
                            color = ShenTextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceTab(viewModel: AssistantViewModel, isListening: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "listeningAnimation")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micPulse"
    )

    val waveAnim1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isListening) 1f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave1"
    )

    val waveAnim2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = if (isListening) 0.9f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave2"
    )

    val waveAnim3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isListening) 1f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave3"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("VOICE ASSISTANT MATRIX", color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(28.dp))

        // Sound Wave Visualizer when listening
        Row(
            modifier = Modifier.height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val barHeights = listOf(waveAnim1, waveAnim2, waveAnim3, waveAnim2, waveAnim1)
            barHeights.forEach { scale ->
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height((36 * scale).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isListening) ShenEmerald else ShenTextSecondary.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mic Button with Pulse Rings
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size((140 * pulseScale).dp)
                        .clip(RoundedCornerShape(70.dp))
                        .background(ShenEmerald.copy(alpha = 0.15f))
                )
            }
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(65.dp))
                    .background(if (isListening) ShenEmerald.copy(alpha = 0.25f) else ShenCardDark)
                    .border(3.dp, if (isListening) ShenEmerald else ShenCyan, RoundedCornerShape(65.dp))
                    .clickable { viewModel.toggleVoiceListening() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                    contentDescription = "Microphone",
                    tint = if (isListening) ShenEmerald else ShenCyan,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isListening) "🎙️ LISTENING... Speak your command to SHEN" else "Tap microphone to initiate voice conversation",
            color = if (isListening) ShenEmerald else ShenTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isListening) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ShenCardDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Voice Neural Prompts", color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• \"SHEN, what is the current system status?\"", color = ShenTextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• \"Calculate orbital trajectory for satellite launch\"", color = ShenTextPrimary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ToolsTab(viewModel: AssistantViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("AGENTIC TOOLKIT", color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))

        ToolItem(
            title = "Neural Web Search Grounding",
            description = "Fetch real-time data indexed via Gemini search tools.",
            onClick = {
                viewModel.setTab(1)
                viewModel.sendMessage("Search Google for latest breakthrough in artificial intelligence.", "search")
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ToolItem(
            title = "Geospatial Maps Grounding",
            description = "Query location intelligence and coordinates.",
            onClick = {
                viewModel.setTab(1)
                viewModel.sendMessage("Provide geospatial analysis for San Francisco tech sector.", "maps")
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ToolItem(
            title = "System Diagnostics & Self-Test",
            description = "Verify local memory, CPU load, and neural link integrity.",
            onClick = {
                viewModel.setTab(1)
                viewModel.sendMessage("Execute system diagnostics report.", "chat")
            }
        )
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
