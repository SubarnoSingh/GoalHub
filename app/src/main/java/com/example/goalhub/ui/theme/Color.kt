package com.example.goalhub.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Light palette — soft, off-white, premium productivity feel
// ---------------------------------------------------------------------------
private val LightInk = Color(0xFF23222A)            // primary text
private val LightMutedInk = Color(0xFF7B7883)       // secondary text
private val LightFaintInk = Color(0xFFA9A6B0)       // tertiary text / hints
private val LightCanvas = Color(0xFFFAF9F7)         // app background (soft off-white)
private val LightCardSurface = Color(0xFFFFFFFF)    // card surface
private val LightSoftLine = Color(0xFFEDEAE4)       // card borders / dividers
private val LightSoftShadow = Color(0x1A2B2A32)     // subtle shadows
private val LightPrimary = Color(0xFF5B6BF0)        // soft indigo
private val LightPrimarySoft = Color(0xFFE9EBFE)    // indigo tint

// ---------------------------------------------------------------------------
// Dark palette — same hues, tuned for dark surfaces
// ---------------------------------------------------------------------------
internal val DarkCanvas = Color(0xFF121216)          // app background
internal val DarkSurface = Color(0xFF1C1C23)         // card surface
internal val DarkSurfaceHigh = Color(0xFF24242D)     // raised surfaces / input fields
internal val DarkLine = Color(0xFF2E2E38)            // borders / dividers
internal val DarkInk = Color(0xFFECEBF0)             // primary text
internal val DarkMuted = Color(0xFF9B98A6)           // secondary text
internal val DarkFaintInk = Color(0xFF6C6978)        // tertiary text / hints
internal val DarkSoftShadow = Color(0x66000000)      // deeper shadow to stay visible on dark
internal val DarkPrimary = Color(0xFF8A93FF)         // brighter indigo for dark backgrounds
internal val DarkPrimarySoft = Color(0xFF3A3F8C)     // indigo tint (deep, matches dark primary)

/**
 * The subset of the palette that flips with the theme. Accent colors
 * (success/warning/danger, status + priority chips) intentionally stay the
 * same in both themes — they are used on gradients and as small accents where
 * a constant hue reads better.
 */
data class GoalHubPalette(
    val ink: Color,
    val mutedInk: Color,
    val faintInk: Color,
    val canvas: Color,
    val cardSurface: Color,
    val softLine: Color,
    val softShadow: Color,
    val primary: Color,
    val primarySoft: Color,
)

val LightGoalHubPalette = GoalHubPalette(
    ink = LightInk,
    mutedInk = LightMutedInk,
    faintInk = LightFaintInk,
    canvas = LightCanvas,
    cardSurface = LightCardSurface,
    softLine = LightSoftLine,
    softShadow = LightSoftShadow,
    primary = LightPrimary,
    primarySoft = LightPrimarySoft,
)

val DarkGoalHubPalette = GoalHubPalette(
    ink = DarkInk,
    mutedInk = DarkMuted,
    faintInk = DarkFaintInk,
    canvas = DarkCanvas,
    cardSurface = DarkSurface,
    softLine = DarkLine,
    softShadow = DarkSoftShadow,
    primary = DarkPrimary,
    primarySoft = DarkPrimarySoft,
)

val LocalGoalHubColors = staticCompositionLocalOf { LightGoalHubPalette }

// ---------------------------------------------------------------------------
// Adaptive accessors — resolve to the active theme's palette, so existing
// call sites (`color = Ink`) automatically follow light/dark mode.
// ---------------------------------------------------------------------------
val Ink: Color @Composable get() = LocalGoalHubColors.current.ink
val MutedInk: Color @Composable get() = LocalGoalHubColors.current.mutedInk
val FaintInk: Color @Composable get() = LocalGoalHubColors.current.faintInk
val Canvas: Color @Composable get() = LocalGoalHubColors.current.canvas
val CardSurface: Color @Composable get() = LocalGoalHubColors.current.cardSurface
val SoftLine: Color @Composable get() = LocalGoalHubColors.current.softLine
val SoftShadow: Color @Composable get() = LocalGoalHubColors.current.softShadow
val Primary: Color @Composable get() = LocalGoalHubColors.current.primary
val PrimarySoft: Color @Composable get() = LocalGoalHubColors.current.primarySoft

// ---------------------------------------------------------------------------
// Shared accent colors (used on gradients / as accents — constant in both themes)
// ---------------------------------------------------------------------------
val Secondary = Color(0xFF10B3A3)     // calm teal
val Success = Color(0xFF2EBC7E)
val Warning = Color(0xFFF0A63C)
val Danger = Color(0xFFE85D5D)

// Status chip colors
val StatusNotStarted = Color(0xFF9AA0A6)
val StatusInProgress = Color(0xFF4F8DF6)
val StatusCompleted = Success
val StatusPaused = Color(0xFFF0A63C)
val StatusArchived = Color(0xFF8C8894)
val StatusOverdue = Danger

// Priority colors
val PriorityLow = Color(0xFF8C8894)
val PriorityMedium = Color(0xFF4F8DF6)
val PriorityHigh = Color(0xFFF0903C)
val PriorityCritical = Danger
