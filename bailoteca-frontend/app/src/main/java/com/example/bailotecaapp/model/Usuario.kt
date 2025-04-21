package com.example.bailotecaapp.model

import java.time.LocalDate

data class Usuario(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val rol: String,
    val fotoPerfil: String?,
    val telefono: String?,
    val direccion: String?,
    val fechaNacimiento: String?, // JSON lo devuelve como String (yyyy-MM-dd)
    val genero: String?,
    val dni: String?,
    val fechaRegistro: String?,   // También vendrá como String
    val activo: Boolean,
    val pagado: Boolean
)
