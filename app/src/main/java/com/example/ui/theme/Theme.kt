package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD0BCFF), // Bento Lavender Purple
    secondary = Color(0xFF4F378B), // Bento Dark Purple
    tertiary = Color(0xFFEADDFF), // Bento Light Violet Highlight
    background = Color(0xFF1C1B1F), // Bento Deep Charcoal Base
    surface = Color(0xFF313033), // Bento Slate Grey Card Surface
    onPrimary = Color(0xFF381E72), // Bento Dark Purple Text
    onSecondary = Color(0xFFEADDFF),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
  )

private val LightColorScheme = DarkColorScheme // Keep it styled with dark sports-bento look

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Force beautiful Bento theme consistently across devices
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
