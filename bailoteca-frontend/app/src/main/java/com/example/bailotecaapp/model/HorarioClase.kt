package com.example.bailotecaapp.model

data class HorarioClase(
    val id: Long,
    val diaSemana: String,
    val horaInicio: String,  // Formato: "HH:mm:ss"
    val horaFin: String,
    val clase: Clase? = null // Para evitar ciclos
)
