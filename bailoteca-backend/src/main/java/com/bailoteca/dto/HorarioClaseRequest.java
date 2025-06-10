package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * DTO para representar un horario dentro de la creación o edición de clase.
 * Incluye el campo `id` para soportar edición sin duplicaciones.
 * Este objeto se utiliza para enviar datos desde el cliente al servidor
 * para crear o actualizar un horario de clase específico.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see HorarioClaseRequest
 * @see ClaseRequest
 * @see ClaseService
 * @see ClaseRepo
 * @see Usuario
 * @see UsuarioRepo
 * @see UsuarioDetails
 * 
 */
@Data
public class HorarioClaseRequest {
    private Long id;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}