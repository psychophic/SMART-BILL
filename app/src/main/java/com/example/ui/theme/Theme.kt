package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BiriYellow,
    onPrimary = Color.Black,
    primaryContainer = BiriYellowDark,
    onPrimaryContainer = Color.Black,
    secondary = BiriRed,
    onSecondary = Color.White,
    secondaryContainer = BiriRedContainer,
    onSecondaryContainer = BiriRedLight,
    tertiary = BiriYellowLight,
    onTertiary = Color.Black,
    background = BiriBlack,
    onBackground = Color.White,
    surface = BiriBlackCard,
    onSurface = Color.White,
    surfaceVariant = BiriBlackElevated,
    onSurfaceVariant = Color(0xFFDDDDDD),
    error = BiriRed,
    onError = Color.White,
    errorContainer = BiriRedContainer,
    onErrorContainer = BiriRedLight
)

private val LightColorScheme = lightColorScheme(
    primary = BiriRedDark,
    onPrimary = Color.White,
    primaryContainer = BiriYellowSubtle,
    onPrimaryContainer = Color.Black,
    secondary = BiriYellowDark,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFF3E0),
    onSecondaryContainer = Color.Black,
    tertiary = BiriRed,
    onTertiary = Color.White,
    background = Color(0xFFF9F9F9),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF444444),
    error = BiriRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = BiriRedDark
)

@Composable
fun MyApplicationTheme(
    userThemeMode: String = "DARK", // Default to Dark which best showcases Yellow, Red & Black
    content: @Composable () -> Unit,
) {
    val darkTheme = when (userThemeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkCustomColors else LightCustomColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
