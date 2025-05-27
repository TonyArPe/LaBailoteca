package com.example.bailotecaapp.model.dto

/**
 * DTO para actualizar solo los campos editables del perfil de usuario.
 */
data class UsuarioUpdateRequest(
    val nombre: String,
    val telefono: String?,
    val direccion: String?,
    val fechaNacimiento: String?,
    val genero: String?,
    val fotoPerfil: String?
)