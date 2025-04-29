package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.Rol
import java.time.LocalDate

data class Usuario(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val contrasenna: String,
    val rol: Rol,
    val fotoPerfil: String?,
    val telefono: String?,
    val direccion: String?,
    val fechaNacimiento: String?, // formato ISO-8601
    val genero: String?,
    val dni: String?,
    val fechaRegistro: String?, // formato ISO-8601
    val activo: Boolean,
    val pagado: Boolean
)