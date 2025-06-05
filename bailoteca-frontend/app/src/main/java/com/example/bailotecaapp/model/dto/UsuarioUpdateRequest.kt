package com.example.bailotecaapp.model.dto

import com.example.bailotecaapp.model.enums.Rol
import kotlinx.serialization.Serializable

/**
 * DTO usado para actualizar los datos de un usuario desde el frontend.
 * Admite campos opcionales y completos del modelo Usuario.
 */
@Serializable
data class UsuarioUpdateRequest(
    val nombre: String,
    val apellido: String? = null,
    val correo: String,
    val contrasenna: String,
    val rol: Rol,
    val telefono: String? = null,
    val direccion: String? = null,
    val fechaNacimiento: String? = null,
    val genero: String? = null,
    val fotoPerfil: String? = null,
    val activo: Boolean = false,
    val pagado: Boolean = false
)