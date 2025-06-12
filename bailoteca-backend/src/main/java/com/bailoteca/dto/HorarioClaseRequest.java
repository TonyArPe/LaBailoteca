package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * Clase que representa una solicitud para crear o actualizar un horario de clase.
 * Contiene información sobre el día de la semana, hora de inicio y hora de fin.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@Data
public class HorarioClaseRequest {
    private Long id;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}