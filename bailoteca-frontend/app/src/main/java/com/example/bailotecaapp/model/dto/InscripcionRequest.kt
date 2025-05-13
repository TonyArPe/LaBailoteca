package com.example.bailotecaapp.model.dto

import kotlinx.serialization.Serializable

/**
 * DTO que encapsula el ID de la clase para solicitar la inscripción.
 */
@Serializable
data class InscripcionRequest(
    val claseId: Long
)