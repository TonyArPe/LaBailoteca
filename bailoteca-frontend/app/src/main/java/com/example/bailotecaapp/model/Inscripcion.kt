package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.EstadoInscripcion

data class Inscripcion(
    val id: Long,
    val claseId: Clase,
    val usuarioId: Usuario,
    val fechaInscripcion: String,
    val estado: EstadoInscripcion
)