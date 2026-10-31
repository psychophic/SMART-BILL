package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Iconic BiriKhor Theme Palette: Yellow, Red, Black
val BiriYellow = Color(0xFFFFD000)       // Vibrant Electric Yellow
val BiriYellowLight = Color(0xFFFFE57F)  // Light Golden Yellow
val BiriYellowDark = Color(0xFFFFAB00)   // Deep Golden Yellow
val BiriYellowSubtle = Color(0xFFFFF9C4) // Soft Pastel Yellow

val BiriRed = Color(0xFFE53935)          // Fiery Crimson Red
val BiriRedDark = Color(0xFFB71C1C)      // Deep Maroon Red
val BiriRedLight = Color(0xFFFF8A80)     // Bright Red Tint
val BiriRedContainer = Color(0xFF3B1212) // Dark Red Container

val BiriBlack = Color(0xFF0F0F0F)        // Jet Black Background
val BiriBlackCard = Color(0xFF1E1E1E)    // Dark Charcoal Card
val BiriBlackElevated = Color(0xFF282828)// Elevated Dark Surface
val BiriBorderDark = Color(0xFF333333)   // Subtle Border

val BiriWhite = Color(0xFFFFFFFF)
val BiriTextMuted = Color(0xFFAAAAAA)
val BiriGreen = Color(0xFF4CAF50)

data class AppCustomColors(
    val isDark: Boolean,
    val background: Color,
    val cardBackground: Color,
    val elevatedBackground: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentYellow: Color,
    val accentRed: Color,
    val accentGreen: Color
)

val DarkCustomColors = AppCustomColors(
    isDark = true,
    background = Color(0xFF101010),
    cardBackground = Color(0xFF1E1E1E),
    elevatedBackground = Color(0xFF282828),
    border = Color(0xFF383838),
    textPrimary = Color.White,
    textSecondary = Color(0xFFCCCCCC),
    textMuted = Color(0xFF888888),
    accentYellow = BiriYellow,
    accentRed = BiriRed,
    accentGreen = BiriGreen
)

val LightCustomColors = AppCustomColors(
    isDark = false,
    background = Color(0xFFF4F5F7),
    cardBackground = Color(0xFFFFFFFF),
    elevatedBackground = Color(0xFFE9ECEF),
    border = Color(0xFFD6D9DE),
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF444444),
    textMuted = Color(0xFF757575),
    accentYellow = Color(0xFFD97706), // Deep Amber for light theme contrast
    accentRed = Color(0xFFD32F2F),
    accentGreen = Color(0xFF2E7D32)
)

val LocalAppColors = staticCompositionLocalOf { DarkCustomColors }

object AppTheme {
    val colors: AppCustomColors
        @Composable
        get() = LocalAppColors.current
}
