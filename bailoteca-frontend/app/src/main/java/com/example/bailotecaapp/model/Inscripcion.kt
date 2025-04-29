package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.EstadoInscripcion

data class Inscripcion(
    val id: Long,
    val usuario: Usuario,
    val clase: Clase,
    val fechaInscripcion: String,
    val estado: EstadoInscripcion
)