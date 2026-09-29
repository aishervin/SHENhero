package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screen.MainScreen
import com.example.ui.theme.ShenHeroTheme
import com.example.ui.viewmodel.AssistantViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShenHeroTheme {
                val viewModel: AssistantViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
