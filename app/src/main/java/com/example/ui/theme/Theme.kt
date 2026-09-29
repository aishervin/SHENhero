package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ShenDarkColorScheme = darkColorScheme(
    primary = ShenCyan,
    secondary = ShenEmerald,
    tertiary = ShenPurple,
    background = ShenBgDark,
    surface = ShenSurfaceDark,
    surfaceVariant = ShenCardDark,
    onPrimary = ShenBgDark,
    onSecondary = ShenBgDark,
    onBackground = ShenTextPrimary,
    onSurface = ShenTextPrimary,
    error = ShenError
)

@Composable
fun ShenHeroTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShenDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ShenHeroTheme(content = content)
}
