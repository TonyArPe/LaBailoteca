package com.example.bailotecaapp.model

data class Clase(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val profesor: Usuario,
    val videoPresentacion: String,
    val horarioClases: List<HorarioClase>,
    val inscritos: List<Usuario> = emptyList()
)
