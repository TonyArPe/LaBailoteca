package com.example.bailotecaapp.model.dto

import com.example.bailotecaapp.model.enums.Dificultad
import kotlinx.serialization.Serializable

@Serializable
data class ClaseRequest(
    val nombre: String,
    val descripcion: String,
    val videoPresentacion: String,
    val ubicacion: String,
    val dificultad: Dificultad?,
    val horarioClases: List<HorarioClaseRequest>
)
