package com.bailoteca.dto;

import com.bailoteca.models.enums.EstadoEvento;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Clase que representa la respuesta de un evento.
 * Contiene información sobre el evento, incluyendo su estado,
 * si es público, y detalles del organizador.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
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