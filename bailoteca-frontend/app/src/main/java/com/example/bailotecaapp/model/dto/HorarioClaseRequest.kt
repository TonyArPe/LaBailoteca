package com.example.bailotecaapp.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class HorarioClaseRequest(
    val diaSemana: String,
    val horaInicio: String,
    val horaFin: String
)