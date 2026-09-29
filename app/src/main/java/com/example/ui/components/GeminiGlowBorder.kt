package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GeminiGlowCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    containerColor: Color = Color(0xFF0A0A0A),
    borderWidth: Dp = 1.5.dp,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "geminiBorder")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val colors = listOf(
        Color(0xFF00E5FF), // Cyan
        Color(0xFF10B981), // Emerald
        Color(0xFF3B82F6), // Blue
        Color(0xFF8B5CF6), // Purple
        Color(0xFF00E5FF)  // Cyan
    )

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                val brush = Brush.sweepGradient(
                    colors = colors,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
                drawRect(
                    brush = brush,
                    size = size
                )
            }
            .padding(borderWidth)
            .clip(shape)
            .background(containerColor)
    ) {
        content()
    }
}
