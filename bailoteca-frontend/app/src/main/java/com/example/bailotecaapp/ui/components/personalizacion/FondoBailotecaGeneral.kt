package com.example.bailotecaapp.ui.components.personalizacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun FondoBailotecaGeneral(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF181818),     // Negro suave
                        Color(0xFFDA1884),     // Fucsia
                        Color(0xFF78BE20),     // Verde Bailoteca
                    )
                )
            )
    ) {
        content()
    }
}