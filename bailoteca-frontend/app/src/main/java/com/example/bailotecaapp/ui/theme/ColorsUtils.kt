package com.example.bailotecaapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.bailotecaapp.model.enums.Dificultad

/**
 * Devuelve un color representativo para cada dificultad.
 *
 * @param dificultad Dificultad de la clase
 */
fun dificultadColor(dificultad: Dificultad?): Color {
    return when (dificultad) {
        Dificultad.INICIAL -> Lima
        Dificultad.INTERMEDIO -> Color(0xFFFF9800)
        Dificultad.AVANZADO -> Magenta
        null -> Color.Gray
    }
}
