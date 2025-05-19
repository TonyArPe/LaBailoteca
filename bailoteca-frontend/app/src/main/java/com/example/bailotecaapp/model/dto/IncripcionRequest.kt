package com.example.bailotecaapp.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class InscripcionRequest(
    val usuarioId: Long,
    val claseId: Long
)