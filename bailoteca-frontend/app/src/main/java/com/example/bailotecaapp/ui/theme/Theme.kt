package com.example.bailotecaapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BailotecaLightColorScheme = lightColorScheme(
    primary = Color(0xFFEC007F),
    secondary = Color(0xFF95D600),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

val BailotecaDarkColorScheme = darkColorScheme(
    primary = Color(0xFFEC007F),
    secondary = Color(0xFF95D600),
    background = Color(0xFF1C1C1C),
    surface = Color(0xFF2A2A2A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun BailotecaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) BailotecaDarkColorScheme else BailotecaLightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
