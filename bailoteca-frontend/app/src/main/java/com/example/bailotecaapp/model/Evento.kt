package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.EstadoEvento

data class Evento(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val fecha: String, // formato ISO
    val lugar: String,
    val estado: EstadoEvento,
    val organizador: Usuario,
    val publico: Boolean
)
