package com.example.bailotecaapp.model

/**
 * Modelo de datos que representa un evento recibido desde el backend.
 * Corresponde directamente con EventoDTO del backend.
 */
data class Evento(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val fecha: String, // ISO 8601
    val lugar: String,
    val publico: Boolean,
    val nombreOrganizador: String,
    val organizadorId: Long? = null,
    val urlImagen: String? = null
)