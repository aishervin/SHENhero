package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ShenCyan
import com.example.ui.theme.ShenEmerald
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AvatarView(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    isThinking: Boolean = false,
    isTyping: Boolean = false,
    isSleeping: Boolean = false
) {
    // Blink animation state
    val blinkProgress = remember { Animatable(1f) }

    // Random blinking effect
    LaunchedEffect(isSleeping, isThinking, isTyping) {
        if (!isSleeping && !isThinking) {
            while (isActive) {
                delay(3000 + (Math.random() * 3000).toLong())
                if (!isSleeping && !isThinking) {
                    blinkProgress.animateTo(0.1f, animationSpec = tween(120))
                    blinkProgress.animateTo(1f, animationSpec = tween(120))
                }
            }
        }
    }

    // Thinking pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "avatarThinking")
    val thinkingOscillation by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinkingOscillation"
    )

    // Typing reading oscillation
    val typingOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typingOffset"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1f2937), Color(0xFF0b0f19))
                )
            )
            .border(2.dp, ShenCyan.copy(alpha = 0.6f), RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.85f)) {
            val w = size.toPx() * 0.85f
            val h = w

            // Calculate eye scales and positions
            var leftScale = 1f
            var rightScale = 1f

            if (isThinking) {
                val scaleAmount = 0.35f
                leftScale = 1f - (scaleAmount * (thinkingOscillation + 1f) / 2f)
                rightScale = 1f - (scaleAmount * (-thinkingOscillation + 1f) / 2f)
            }

            val eyeWidth = w * 0.28f
            val eyeHeight = h * 0.32f
            val currentBlink = if (isSleeping) 0.15f else blinkProgress.value

            val lX = w * 0.22f + (if (isTyping) typingOffset else 0f)
            val rX = w * 0.52f + (if (isTyping) typingOffset else 0f)
            val eY = h * 0.34f

            if (isSleeping) {
                // Draw sleeping bars
                drawRoundRect(
                    color = ShenCyan,
                    topLeft = Offset(lX, h * 0.48f),
                    size = Size(eyeWidth, eyeHeight * 0.3f),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                drawRoundRect(
                    color = ShenCyan,
                    topLeft = Offset(rX, h * 0.48f),
                    size = Size(eyeWidth, eyeHeight * 0.3f),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            } else {
                // Left Eye
                drawCyberEye(
                    topLeft = Offset(lX, eY),
                    size = Size(eyeWidth, eyeHeight * currentBlink * leftScale),
                    color = ShenCyan
                )

                // Right Eye
                drawCyberEye(
                    topLeft = Offset(rX, eY),
                    size = Size(eyeWidth, eyeHeight * currentBlink * rightScale),
                    color = ShenCyan
                )
            }
        }
    }
}

private fun DrawScope.drawCyberEye(topLeft: Offset, size: Size, color: Color) {
    if (size.height <= 0f) return

    // Draw eye background / glow
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(10.dp.toPx())
    )

    // Draw scanlines on the eye
    val step = 4.dp.toPx()
    var currentY = topLeft.y + 2f
    val maxY = topLeft.y + size.height - 2f
    while (currentY < maxY) {
        drawLine(
            color = Color.Black.copy(alpha = 0.35f),
            start = Offset(topLeft.x, currentY),
            end = Offset(topLeft.x + size.width, currentY),
            strokeWidth = 1.5f
        )
        currentY += step
    }
}
