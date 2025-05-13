package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.EstadoInscripcion
import kotlinx.serialization.Serializable

@Serializable
data class Inscripcion(
    val id: Long,
    val clase: Clase,
    val usuario: Usuario,
    val fechaInscripcion: String,
    val estado: EstadoInscripcion
)