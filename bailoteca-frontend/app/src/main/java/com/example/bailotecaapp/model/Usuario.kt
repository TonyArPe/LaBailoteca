package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.Rol
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val id: Long,
    val nombre: String,
    val apellido: String? = null,
    val correo: String,
    val contrasenna: String? = null,
    val rol: Rol,
    val fotoPerfil: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val fechaNacimiento: String? = null,
    val genero: String? = null,
    val dni: String? = null,
    val fechaRegistro: String,
    val activo: Boolean,
    val pagado: Boolean
)

