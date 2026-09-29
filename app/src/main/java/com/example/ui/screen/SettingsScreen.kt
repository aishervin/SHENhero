package com.example.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShenCardDark
import com.example.ui.theme.ShenCyan
import com.example.ui.theme.ShenEmerald
import com.example.ui.theme.ShenTextPrimary
import com.example.ui.theme.ShenTextSecondary
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun SettingsScreen(viewModel: AssistantViewModel) {
    val apiKey by viewModel.currentApiKey.collectAsState()
    var input by remember(apiKey) { mutableStateOf(apiKey) }
    var keyVisible by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var confirmClearHistory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(Icons.Default.Security, contentDescription = null, tint = ShenCyan)
        Text(
            "Gemini connection",
            color = ShenCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            "Add your own Gemini API key. It is encrypted on this device and is not bundled into the app.",
            color = ShenTextSecondary,
            fontSize = 13.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = ShenCardDark),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, ShenCyan.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; saved = false },
                    label = { Text("Gemini API key") },
                    leadingIcon = { Icon(Icons.Default.SmartToy, contentDescription = null, tint = ShenCyan) },
                    trailingIcon = {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Icon(
                                imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (keyVisible) "Hide key" else "Show key",
                                tint = ShenTextSecondary
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                    onClick = { viewModel.updateApiKey(input); saved = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShenCyan.copy(alpha = 0.18f))
                ) {
                    Text("Save securely", color = ShenCyan, fontWeight = FontWeight.Bold)
                }
                if (saved) {
                    Text("Key saved on this device.", color = ShenEmerald, fontSize = 12.sp)
                }
            }
        }
        Text(
            "For a public multi-user service, use a protected server rather than sharing one API key with every installation. Set usage limits for your key in Google AI Studio.",
            color = ShenTextSecondary,
            fontSize = 12.sp
        )
        TextButton(onClick = { confirmClearHistory = true }) {
            Text("Clear chat history", color = ShenCyan)
        }
    }

    if (confirmClearHistory) {
        AlertDialog(
            onDismissRequest = { confirmClearHistory = false },
            title = { Text("Clear chat history?") },
            text = { Text("This permanently removes saved conversations from this device.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearChat(); confirmClearHistory = false }) {
                    Text("Clear", color = ShenCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearHistory = false }) {
                    Text("Cancel", color = ShenTextSecondary)
                }
            }
        )
    }
}
