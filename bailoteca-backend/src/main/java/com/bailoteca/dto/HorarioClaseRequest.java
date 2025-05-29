package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * DTO para representar un horario dentro de la creación/edición de clase.
 */
@Data
public class HorarioClaseRequest {
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}