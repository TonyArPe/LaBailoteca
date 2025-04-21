package com.example.bailotecaapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ========== Colores personalizados ==========
private val PurplePrimary = Color(0xFF7B1FA2)   // Morado del logo
private val PurpleAccent = Color(0xFFD500F9)   // Fucsia vibrante
private val LightBackground = Color(0xFFFFFFFF)
private val LightSurface = Color(0xFFF1F1F1)
private val DarkBackground = Color(0xFF1E1E1E)
private val DarkSurface = Color(0xFF2A2A2A)
private val TextDark = Color(0xFF212121)
private val TextLight = Color(0xFFEDEDED)

// ========== Esquemas de colores para cada modo ==========
private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    secondary = PurpleAccent,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,       // Texto sobre botón morado
    onSecondary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    secondary = PurpleAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextLight,
    onSurface = TextLight
)

// ========== Función principal del tema ==========
@Composable
fun BailotecaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta si el usuario está en modo oscuro
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,  // Enlaza con Type
        content = content
    )
}
