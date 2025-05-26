package com.example.bailotecaapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Magenta,
    secondary = Lima,
    background = Blanco,
    surface = Color(0xFFF1F1F1),
    onPrimary = Blanco,
    onSecondary = Negro,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro
)

private val DarkColorScheme = darkColorScheme(
    primary = Magenta,
    secondary = Lima,
    background = GrisOscuro,
    surface = GrisSurface,
    onPrimary = Blanco,
    onSecondary = Negro,
    onBackground = TextoClaro,
    onSurface = TextoClaro
)

@Composable
fun BailotecaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}