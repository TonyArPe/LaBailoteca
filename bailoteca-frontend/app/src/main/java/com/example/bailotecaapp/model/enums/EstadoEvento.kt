package com.example.bailotecaapp.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EstadoEvento {
    @SerialName("ACTIVO")
    ACTIVO,
    @SerialName("CANCELADO")
    CANCELADO,
    @SerialName("POSPUESTO")
    POSPUESTO
}
