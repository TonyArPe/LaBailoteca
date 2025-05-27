package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.Rol
import kotlinx.serialization.Serializable

/**
 * Representación del usuario desde el backend.
 * Se adapta para manejar campos nulos opcionales y formatos de fecha en texto ISO.
 */
@Serializable
data class Usuario(
    val id: Long? = null,
    val nombre: String,
    val apellido: String = "",
    val correo: String,
    val contrasenna: String,
    val rol: Rol = Rol.USUARIO,
    val fotoPerfil: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val fechaNacimiento: String? = null,
    val genero: String? = null,
    val dni: String? = null,
    val fechaRegistro: String? = null,
    val activo: Boolean = false,
    val pagado: Boolean = false
)
