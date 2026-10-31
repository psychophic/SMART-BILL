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

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = LightAmberTea,
    secondary = MildCoffee,
    tertiary = AmberTea,
    background = EspressoBlack,
    surface = CardBackgroundDark,
    onPrimary = Color.Black,
    onBackground = Color(0xFFF5EBE6),
    onSurface = Color(0xFFEFE5DF)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AmberTea,
    secondary = EspressoBrown,
    tertiary = MildCoffee,
    background = WarmCream,
    surface = CardBackgroundLight,
    onPrimary = Color.White,
    onBackground = Color(0xFF2C1E18),
    onSurface = Color(0xFF3E2D24)
  )

@Composable
fun MyApplicationTheme(
  userThemeMode: String = "SYSTEM",
  content: @Composable () -> Unit,
) {
  val darkTheme = when (userThemeMode) {
    "LIGHT" -> false
    "DARK" -> true
    else -> isSystemInDarkTheme()
  }

  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
