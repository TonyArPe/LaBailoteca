package com.bailoteca.dto;

import com.bailoteca.models.enums.EstadoEvento;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de salida para eventos.
 * Expone solo los datos necesarios al frontend.
 * * Este objeto se utiliza para enviar datos de eventos
 * desde el servidor al cliente, incluyendo detalles
 * como el nombre, descripción, fecha, lugar,
 * estado, si es público y el nombre del organizador.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see EstadoEvento
 * @see EventoResponse
 * @see EventoService
 * @see EventoRepo
 * @see Usuario
 * @see UsuarioRepo
 * @see UsuarioDetails
 * @see UsuarioDetailsService
 * @see EventoRequest
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