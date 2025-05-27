package com.example.bailotecaapp.model.dto

import kotlinx.serialization.Serializable

/**
 * DTO que representa la solicitud de inscripción a una clase.
 * Se envía al backend cuando un usuario desea inscribirse.
 */
@Serializable
data class InscripcionRequest(
    val usuarioId: Long,
    val claseId: Long
)