package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrightCyanAccent,
    secondary = HologramBlue,
    tertiary = SoftGreenStable,
    background = CyberBlack,
    surface = DarkGreySurface,
    onPrimary = CyberBlack,
    onSecondary = CyberBlack,
    onBackground = LightSlateText,
    onSurface = LightSlateText,
    error = AlertRedCollapse,
    errorContainer = AlertRedCollapse
)

private val LightColorScheme = lightColorScheme(
    primary = BrightCyanAccent,
    secondary = HologramBlue,
    tertiary = SoftGreenStable,
    background = CyberBlack, // Keep it atmospheric and dark even in light mode to preserve the "Second Brain Lag" cybersecurity vibe!
    surface = DarkGreySurface,
    onBackground = LightSlateText,
    onSurface = LightSlateText,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force custom atmospheric dark theme by default
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // Force dark theme for maximum cybersecurity focus-tracking immersion!

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
