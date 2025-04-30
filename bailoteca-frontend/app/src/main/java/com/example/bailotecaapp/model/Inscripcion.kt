package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.EstadoInscripcion

data class Inscripcion(
    val id: Long,
    val claseId: Long,
    val usuarioId: Long,
    val fechaInscripcion: String,
    val estado: EstadoInscripcion
)