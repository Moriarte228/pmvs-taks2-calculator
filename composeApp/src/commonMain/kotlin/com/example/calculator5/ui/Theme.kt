package com.example.calculator5.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF10B981)
private val GreenDeep = Color(0xFF047857)
private val GreenLight = Color(0xFFD1FAE5)

private val LightScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDeep,
    secondary = Color(0xFF334155),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF052E16),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = GreenLight,
    secondary = Color(0xFF94A3B8),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
