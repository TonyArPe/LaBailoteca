package com.example.bailotecaapp.model.dto

import kotlinx.serialization.Serializable

/**
 * Representa la petición para registrar o actualizar una asistencia a un evento.
 * Contiene únicamente los datos de estado (asistencia y pago), ya que
 * el usuario y el evento se determinan en el servidor.
 */
@Serializable
data class AsistenciaEventoRequest(
    val asistira: Boolean,
    val pagado: Boolean
)