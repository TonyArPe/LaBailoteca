package com.example.bailotecaapp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual

@Serializable
data class HorarioClase(
    val id: Long,
    val diaSemana: String,
    val horaInicio: String, // "HH:mm:ss"
    val horaFin: String,
    @Contextual val clase: Clase? = null // Evita ciclos infinitos
)