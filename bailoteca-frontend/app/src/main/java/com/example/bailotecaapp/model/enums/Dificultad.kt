package com.example.bailotecaapp.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Dificultad {
    @SerialName("FACIL")
    FACIL,
    @SerialName("MEDIA")
    MEDIA,
    @SerialName("DIFICIL")
    DIFICIL
}