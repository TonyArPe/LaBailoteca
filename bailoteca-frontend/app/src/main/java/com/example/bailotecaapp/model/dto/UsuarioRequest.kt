package com.example.bailotecaapp.model.dto

import com.example.bailotecaapp.model.enums.Rol
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val contrasenna: String,
    val rol: Rol,
    val telefono: String = "",
    val direccion: String = "",
    val fechaNacimiento: String? = null //yyyy-MM-dd
)