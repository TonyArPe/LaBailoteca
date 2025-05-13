package com.example.bailotecaapp.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Dificultad {
    @SerialName("PRINCIPIANTE")
    PRINCIPIANTE,
    @SerialName("INTERMEDIO")
    INTERMEDIO,
    @SerialName("AVANZADO")
    AVANZADO
}