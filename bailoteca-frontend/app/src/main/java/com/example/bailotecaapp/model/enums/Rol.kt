package com.example.bailotecaapp.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Rol {
    @SerialName("ADMIN")
    ADMIN,
    @SerialName("PROFESOR")
    PROFESOR,
    @SerialName("USUARIO")
    USUARIO,
    @SerialName("INVITADO")
    INVITADO
}