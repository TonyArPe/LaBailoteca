package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.Rol
import kotlinx.serialization.Serializable

/**
 * Representación del usuario desde el backend.
 * Se adapta para manejar campos nulos opcionales y formatos de fecha en texto ISO.
 */
@Serializable
data class Usuario(
    val id: Long,
    val nombre: String,
    val apellido: String = "",
    val correo: String,
    val contrasenna: String = "",

    val rol: Rol,

    val fotoPerfil: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val fechaNacimiento: String = "",
    val genero: String = "",
    val dni: String = "",
    val fechaRegistro: String = "",
    val activo: Boolean,
    val pagado: Boolean
)