package com.example.bailotecaapp.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EstadoInscripcion {
    @SerialName("ACTIVADA")
    ACTIVA,
    @SerialName("CANCELADA")
    CANCELADA,
    @SerialName("FINALIZADA")
    FINALIZADA
}