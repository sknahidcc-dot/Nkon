package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.player.AppThemeMode

private val YouTubeFrostedGlassColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = FrostedGlassWhite20,
    onPrimaryContainer = FrostedTextPrimary,
    secondary = YouTubeRedLight,
    onSecondary = Color.White,
    secondaryContainer = FrostedGlassWhite15,
    onSecondaryContainer = FrostedTextPrimary,
    background = FrostedDarkBackground,
    onBackground = FrostedTextPrimary,
    surface = FrostedDarkBackground,
    onSurface = FrostedTextPrimary,
    surfaceVariant = FrostedGlassWhite10,
    onSurfaceVariant = FrostedTextSecondary,
    outline = FrostedGlassBorder,
    outlineVariant = FrostedGlassBorderSubtle
)

private val YouTubeDarkColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeRedDark,
    onPrimaryContainer = Color.White,
    secondary = YouTubeRedLight,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkDivider
)

private val YouTubeAmoledColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeRedDark,
    onPrimaryContainer = Color.White,
    secondary = YouTubeRedLight,
    onSecondary = Color.White,
    background = AmoledBackground,
    onBackground = DarkOnBackground,
    surface = AmoledSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkDivider
)

private val YouTubeLightColorScheme = lightColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeRedLight,
    onPrimaryContainer = Color.White,
    secondary = YouTubeRedDark,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightDivider
)

@Composable
fun VideoPlayerTheme(
    themeMode: AppThemeMode = AppThemeMode.FROSTED_GLASS,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        AppThemeMode.FROSTED_GLASS -> YouTubeFrostedGlassColorScheme
        AppThemeMode.SYSTEM -> if (systemDark) YouTubeFrostedGlassColorScheme else YouTubeLightColorScheme
        AppThemeMode.LIGHT -> YouTubeLightColorScheme
        AppThemeMode.DARK -> YouTubeDarkColorScheme
        AppThemeMode.AMOLED_BLACK -> YouTubeAmoledColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
