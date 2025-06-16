package com.example.bailotecaapp.model.dto

import com.example.bailotecaapp.model.enums.Rol
import kotlinx.serialization.Serializable

/**
 * DTO para usuario que se usa en frontend.
 * Incluye campos visibles y editables para perfil y detalles.
 */
@Serializable
data class UsuarioDTO(
    val id: Long?,
    val nombre: String,
    val apellido: String? = null,
    val correo: String,
    val rol: Rol,
    val telefono: String? = null,
    val direccion: String? = null,
    val fechaNacimiento: String? = null,
    val genero: String? = null,
    val contrasenna: String? = null,
    val fotoPerfil: String? = null,
    val activo: Boolean = false,
    val pagado: Boolean = false
)