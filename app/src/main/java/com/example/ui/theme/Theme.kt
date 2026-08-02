package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SunsetAmber,
    onPrimary = MidnightSlate,
    secondary = ActiveMint,
    onSecondary = MidnightSlate,
    tertiary = SoftCyan,
    background = MidnightSlate,
    onBackground = SoftCyan,
    surface = DeepCoal,
    onSurface = SoftCyan,
    surfaceVariant = DarkSteel,
    onSurfaceVariant = MutedSlate,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme for the premium hands-free look
    dynamicColor: Boolean = false, // Disable dynamic colors to keep our custom theme cohesive
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
