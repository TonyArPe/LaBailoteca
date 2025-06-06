package com.example.bailotecaapp.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class AsistenciaEvento(
    val id: Long,
    @Contextual val evento: Evento,
    @Contextual val usuario: Usuario,
    val asistira: Boolean,
    val pagado: Boolean
)