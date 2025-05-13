package com.example.bailotecaapp.model

import kotlinx.serialization.Serializable

@Serializable
data class InscripcionRequest(
    val usuarioId: Long,
    val claseId: Long
)
