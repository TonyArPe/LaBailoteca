package com.example.bailotecaapp.model.dto

import kotlinx.serialization.Serializable

/**
 * DTO que representa un horario asociado a una clase. Utilizado tanto para crear
 * como para editar horarios sin duplicarlos. El campo `id` es opcional y solo se usa
 * al editar horarios existentes.
 */
@Serializable
data class HorarioClaseRequest(
    val id: Long? = null,
    val diaSemana: String,
    val horaInicio: String,
    val horaFin: String
)
