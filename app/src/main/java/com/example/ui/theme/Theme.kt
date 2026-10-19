package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun buildDarkColorScheme(accentColor: Color, isAmoled: Boolean) = darkColorScheme(
    primary = accentColor,
    onPrimary = Color.White,
    primaryContainer = accentColor.copy(alpha = 0.25f),
    onPrimaryContainer = Color.White,
    secondary = accentColor,
    onSecondary = Color.White,
    tertiary = accentColor.copy(alpha = 0.8f),
    background = if (isAmoled) Color(0xFF000000) else Color(0xFF0E131C),
    onBackground = Color(0xFFF1F5F9),
    surface = if (isAmoled) Color(0xFF000000) else Color(0xFF161C27),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = if (isAmoled) Color(0xFF121212) else Color(0xFF1E2636),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = if (isAmoled) Color(0xFF262626) else Color(0xFF334155),
    outlineVariant = if (isAmoled) Color(0xFF1A1A1A) else Color(0xFF232D3F)
)

private fun buildLightColorScheme(accentColor: Color) = lightColorScheme(
    primary = accentColor,
    onPrimary = Color.White,
    primaryContainer = accentColor.copy(alpha = 0.15f),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = accentColor,
    onSecondary = Color.White,
    tertiary = accentColor.copy(alpha = 0.85f),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "system", // "system", "light", "dark"
    isAmoledDark: Boolean = false,
    accentColor: Color = OperaCrimson,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode.lowercase()) {
        "light" -> false
        "dark" -> true
        else -> systemInDark
    }

    val colorScheme = if (isDark) {
        buildDarkColorScheme(accentColor, isAmoledDark)
    } else {
        buildLightColorScheme(accentColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
