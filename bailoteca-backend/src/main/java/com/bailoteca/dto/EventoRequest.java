package com.bailoteca.dto;

import com.bailoteca.models.enums.EstadoEvento;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de entrada para la creación o edición de eventos.
 * No debe incluir información del organizador directamente.
 */
@Data
public class EventoRequest {

    private String nombre;
    private String descripcion;
    private LocalDateTime fecha;
    private String lugar;
    private EstadoEvento estado;
    private boolean publico;
}