package com.example.bailotecaapp.model

import kotlinx.serialization.Serializable

@Serializable
data class HorarioClase(
    val id: Long,
    val diaSemana: String,
    val horaInicio: String, // "HH:mm:ss"
    val horaFin: String,
    val clase: Clase? = null // Evita ciclos infinitos
)