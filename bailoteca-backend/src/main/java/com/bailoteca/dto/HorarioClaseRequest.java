package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * DTO para representar un horario dentro de la creación o edición de clase.
 * Incluye el campo `id` para soportar edición sin duplicaciones.
 */
@Data
public class HorarioClaseRequest {
    private Long id;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}