package com.example.bailotecaapp.model.dto

import kotlinx.serialization.Serializable

/**
 * DTO para actualizar solo los campos editables del perfil de usuario.
 * No incluye campos sensibles como el rol, id o correo.
 */
@Serializable
data class UsuarioUpdateRequest(
    val nombre: String,
    val apellido: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val fechaNacimiento: String? = null,
    val genero: String? = null,
    val fotoPerfil: String? = null,
    val dni: String? = null // Opcional
)