package com.bailoteca.dto;

import com.bailoteca.models.enums.EstadoEvento;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de entrada para la creación o edición de eventos.
 * No debe incluir información del organizador directamente.
 * Este objeto se utiliza para enviar datos desde el cliente al servidor
 * para crear o actualizar un evento específico.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see EstadoEvento
 * @see EventoDTO
 * @see EventoService
 * @see EventoRepo
 * @see Usuario
 * @see UsuarioRepo
 * @see UsuarioDetails
 * @see UsuarioDetailsService
 * @see EventoRequest
 * @see EventoController
 * @see EventoService
 * @see EventoRepo
 * @see Usuario
 * @see UsuarioRepo
 * @see UsuarioDetails
 * @see UsuarioDetailsService
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