package com.example.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun SettingsScreen(viewModel: AssistantViewModel) {
    val geminiKey by viewModel.currentApiKey.collectAsState()
    val githubKey by viewModel.githubPat.collectAsState()
    val cloudflareKey by viewModel.cloudflareToken.collectAsState()
    val serperKey by viewModel.serperKey.collectAsState()
    val deepseekKey by viewModel.deepseekKey.collectAsState()
    val openaiKey by viewModel.openaiKey.collectAsState()
    val openrouterKey by viewModel.openrouterKey.collectAsState()
    val groqKey by viewModel.groqKey.collectAsState()
    val telegramKey by viewModel.telegramToken.collectAsState()

    var geminiInput by remember { mutableStateOf(geminiKey) }
    var githubInput by remember { mutableStateOf(githubKey) }
    var cloudflareInput by remember { mutableStateOf(cloudflareKey) }
    var serperInput by remember { mutableStateOf(serperKey) }
    var deepseekInput by remember { mutableStateOf(deepseekKey) }
    var openaiInput by remember { mutableStateOf(openaiKey) }
    var openrouterInput by remember { mutableStateOf(openrouterKey) }
    var groqInput by remember { mutableStateOf(groqKey) }
    var telegramInput by remember { mutableStateOf(telegramKey) }

    var showSavedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ShenBgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = "Security", tint = ShenCyan)
                Text(
                    "NEURAL INTEGRATION HUB & API TOKENS",
                    color = ShenCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Configure API keys and tokens for Gemini, GitHub, Cloudflare, Serper, DeepSeek, OpenAI, OpenRouter, Groq, and Telegram Bot.",
                color = ShenTextSecondary,
                fontSize = 12.sp
            )
        }

        // Gemini API Key
        item {
            IntegrationKeyCard(
                title = "Gemini API Key (Primary Neural Engine)",
                icon = Icons.Default.SmartToy,
                value = geminiInput,
                onValueChange = { geminiInput = it },
                onSave = {
                    viewModel.updateApiKey(geminiInput)
                    showSavedMessage = true
                }
            )
        }

        // GitHub PAT
        item {
            IntegrationKeyCard(
                title = "GitHub Personal Access Token (PAT)",
                icon = Icons.Default.Code,
                value = githubInput,
                onValueChange = { githubInput = it },
                onSave = {
                    viewModel.updateIntegrationKey("github", githubInput)
                    showSavedMessage = true
                }
            )
        }

        // Cloudflare Token
        item {
            IntegrationKeyCard(
                title = "Cloudflare API Token",
                icon = Icons.Default.Cloud,
                value = cloudflareInput,
                onValueChange = { cloudflareInput = it },
                onSave = {
                    viewModel.updateIntegrationKey("cloudflare", cloudflareInput)
                    showSavedMessage = true
                }
            )
        }

        // Serper API Key
        item {
            IntegrationKeyCard(
                title = "Serper Search API Key",
                icon = Icons.Default.Search,
                value = serperInput,
                onValueChange = { serperInput = it },
                onSave = {
                    viewModel.updateIntegrationKey("serper", serperInput)
                    showSavedMessage = true
                }
            )
        }

        // DeepSeek API Key
        item {
            IntegrationKeyCard(
                title = "DeepSeek API Key",
                icon = Icons.Default.Psychology,
                value = deepseekInput,
                onValueChange = { deepseekInput = it },
                onSave = {
                    viewModel.updateIntegrationKey("deepseek", deepseekInput)
                    showSavedMessage = true
                }
            )
        }

        // OpenAI API Key
        item {
            IntegrationKeyCard(
                title = "OpenAI API Key",
                icon = Icons.Default.Api,
                value = openaiInput,
                onValueChange = { openaiInput = it },
                onSave = {
                    viewModel.updateIntegrationKey("openai", openaiInput)
                    showSavedMessage = true
                }
            )
        }

        // OpenRouter API Key
        item {
            IntegrationKeyCard(
                title = "OpenRouter API Key",
                icon = Icons.Default.Hub,
                value = openrouterInput,
                onValueChange = { openrouterInput = it },
                onSave = {
                    viewModel.updateIntegrationKey("openrouter", openrouterInput)
                    showSavedMessage = true
                }
            )
        }

        // Groq API Key
        item {
            IntegrationKeyCard(
                title = "Groq API Key",
                icon = Icons.Default.Speed,
                value = groqInput,
                onValueChange = { groqInput = it },
                onSave = {
                    viewModel.updateIntegrationKey("groq", groqInput)
                    showSavedMessage = true
                }
            )
        }

        // Telegram Bot Token
        item {
            IntegrationKeyCard(
                title = "Telegram Bot Token",
                icon = Icons.Default.Send,
                value = telegramInput,
                onValueChange = { telegramInput = it },
                onSave = {
                    viewModel.updateIntegrationKey("telegram", telegramInput)
                    showSavedMessage = true
                }
            )
        }

        if (showSavedMessage) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ShenEmerald.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ShenEmerald)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = "Saved", tint = ShenEmerald)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "✓ Integration key successfully secured in local storage!",
                            color = ShenEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun IntegrationKeyCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ShenCardDark),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ShenCyan.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = ShenCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    color = ShenTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Enter Token / Key") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle visibility",
                            tint = ShenTextSecondary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShenCyan,
                    unfocusedBorderColor = ShenTextSecondary,
                    focusedLabelColor = ShenCyan,
                    cursorColor = ShenCyan,
                    focusedTextColor = ShenTextPrimary,
                    unfocusedTextColor = ShenTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShenCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ShenCyan)
            ) {
                Text("Secure & Authorize", color = ShenCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
