package com.example.goalhub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LightGoalHubPalette.primary,
    onPrimary = Color.White,
    primaryContainer = LightGoalHubPalette.primarySoft,
    onPrimaryContainer = LightGoalHubPalette.ink,
    secondary = Secondary,
    onSecondary = Color.White,
    background = LightGoalHubPalette.canvas,
    onBackground = LightGoalHubPalette.ink,
    surface = LightGoalHubPalette.cardSurface,
    onSurface = LightGoalHubPalette.ink,
    surfaceVariant = Color(0xFFF2F0EC),
    onSurfaceVariant = LightGoalHubPalette.mutedInk,
    outline = LightGoalHubPalette.softLine,
    outlineVariant = LightGoalHubPalette.softLine,
    error = Danger,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = DarkGoalHubPalette.primary,
    onPrimary = Color(0xFF171A3F),
    primaryContainer = DarkGoalHubPalette.primarySoft,
    onPrimaryContainer = Color(0xFFE2E3FF),
    secondary = Color(0xFF4FD6C6),
    onSecondary = Color(0xFF003B36),
    background = DarkGoalHubPalette.canvas,
    onBackground = DarkGoalHubPalette.ink,
    surface = DarkGoalHubPalette.cardSurface,
    onSurface = DarkGoalHubPalette.ink,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkGoalHubPalette.mutedInk,
    outline = DarkGoalHubPalette.softLine,
    outlineVariant = DarkGoalHubPalette.softLine,
    error = Color(0xFFFF8A8A),
    onError = Color(0xFF5C1A1A),
)

@Composable
fun GoalHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkGoalHubPalette else LightGoalHubPalette
    CompositionLocalProvider(LocalGoalHubColors provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = GoalHubTypography,
            shapes = GoalHubShapes,
            content = content,
        )
    }
}
