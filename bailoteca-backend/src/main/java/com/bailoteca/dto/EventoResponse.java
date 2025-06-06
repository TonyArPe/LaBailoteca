package com.bailoteca.dto;

import com.bailoteca.models.enums.EstadoEvento;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de salida para eventos.
 * Expone solo los datos necesarios al frontend.
 */
@Data
public class EventoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fecha;
    private String lugar;
    private EstadoEvento estado;
    private boolean publico;
    private String organizadorNombre;
    private Long organizadorId;
    private String urlImagen;
}