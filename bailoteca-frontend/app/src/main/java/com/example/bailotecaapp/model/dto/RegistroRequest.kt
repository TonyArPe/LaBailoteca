package com.example.bailotecaapp.model.dto

import com.example.bailotecaapp.model.enums.Rol
import kotlinx.serialization.Serializable

/**
 * DTO para enviar datos de registro al backend.
 */
@Serializable
data class RegistroRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val contrasenna: String
)