package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.EstadoEvento

/**
 * Modelo que representa un evento recibido del backend.
 * Corresponde con EventoDTO.
 */
data class Evento(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val fecha: String, // ISO 8601
    val lugar: String,
    val estado: EstadoEvento,
    val publico: Boolean,
    val nombreOrganizador: String
)
