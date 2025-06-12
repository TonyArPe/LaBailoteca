package com.example.bailotecaapp.model.dto

import com.example.bailotecaapp.model.enums.EstadoEvento
import kotlinx.serialization.Serializable

@Serializable
data class EventoRequest(
    val nombre: String,
    val descripcion: String,
    val fecha: String,
    val lugar: String,
    val estado: EstadoEvento,
    val publico: Boolean,
    val imagen: String? = null
)
