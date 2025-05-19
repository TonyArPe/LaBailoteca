package com.example.bailotecaapp.model

import com.example.bailotecaapp.model.enums.Dificultad

data class Clase(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val profesor: Usuario,
    val ubicacion: String,
    val dificultad: Dificultad?,
    val videoPresentacion: String,
    val horarioClases: List<HorarioClase>
)