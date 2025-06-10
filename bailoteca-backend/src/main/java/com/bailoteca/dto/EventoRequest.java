package com.bailoteca.dto;

import com.bailoteca.models.enums.EstadoEvento;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Clase que representa una solicitud para crear o actualizar un evento.
 * Contiene información sobre el nombre, descripción, fecha, lugar,
 * estado del evento y si es público.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@Data
public class EventoRequest {

    private String nombre;
    private String descripcion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;
    
    private String lugar;
    private EstadoEvento estado;
    private boolean publico;
}