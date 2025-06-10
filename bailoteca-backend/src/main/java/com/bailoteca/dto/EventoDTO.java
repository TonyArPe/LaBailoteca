package com.bailoteca.dto;

import com.bailoteca.models.enums.EstadoEvento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para exponer información de eventos al frontend.
 * Este objeto se utiliza para enviar datos de eventos
 * desde el servidor al cliente, incluyendo detalles
 * como el nombre, descripción, fecha, lugar,
 * estado, si es público y el nombre del organizador.
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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fecha;
    private String lugar;
    private EstadoEvento estado;
    private boolean publico;
    private String nombreOrganizador;
}