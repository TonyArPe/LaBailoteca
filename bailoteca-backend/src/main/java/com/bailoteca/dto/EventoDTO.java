package com.bailoteca.dto;

import com.bailoteca.models.enums.EstadoEvento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Clase que representa un evento en la aplicación.
 * Contiene información sobre el nombre, descripción, fecha, lugar,
 * estado del evento, si es público y el nombre del organizador.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
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