package com.example.bailotecaapp.model.dto

import kotlinx.serialization.Serializable

/**
 * DTO para actualizaciones parciales de estado (activo y pagado).
 * Usado por profesores y administradores.
 */
@Serializable
data class UsuarioEstadoUpdateRequest(
    val activo: Boolean? = null,
    val pagado: Boolean? = null
)