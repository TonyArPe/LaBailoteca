package com.bailoteca.dto;

import com.bailoteca.models.evento.Evento;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferir los datos de un evento al frontend.
 * Contiene solo los campos necesarios para visualización.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fecha;
    private String lugar;
    private boolean publico;
    private String estado;
    private String organizadorNombre;

    /**
     * Crea un EventoDTO a partir de una entidad Evento.
     *
     * @param evento la entidad Evento a convertir.
     * @return un nuevo objeto EventoDTO con los datos necesarios para la vista.
     */
    public static EventoDTO from(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setId(evento.getId());
        dto.setNombre(evento.getNombre());
        dto.setDescripcion(evento.getDescripcion());
        dto.setFecha(evento.getFecha());
        dto.setLugar(evento.getLugar());
        dto.setPublico(evento.isPublico());
        dto.setEstado(evento.getEstado().name());
        dto.setOrganizadorNombre(evento.getOrganizador() != null ? evento.getOrganizador().getNombre() : "Desconocido");
        return dto;
    }
}