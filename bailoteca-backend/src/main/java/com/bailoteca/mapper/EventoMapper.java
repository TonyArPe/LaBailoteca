package com.bailoteca.mapper;

import com.bailoteca.dto.EventoDTO;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper para convertir entre la entidad Evento y su representación DTO.
 * Se utiliza tanto para transformar datos que provienen del frontend como para
 * devolver datos enriquecidos desde la base de datos.
 *
 * Esta versión unificada utiliza EventoDTO para entrada y salida.
 * 
 * @author Tony
 */
public class EventoMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convierte una entidad Evento a su DTO correspondiente para mostrar al cliente.
     *
     * @param evento La entidad evento de base de datos.
     * @return Un objeto EventoDTO con los datos preparados para el frontend.
     */
    public static EventoDTO toDTO(Evento evento) {
        return EventoDTO.builder()
                .id(evento.getId())
                .nombre(evento.getNombre())
                .descripcion(evento.getDescripcion())
                .fecha(evento.getFecha().format(FORMATTER)) // ⏱ LocalDateTime -> String
                .lugar(evento.getLugar())
                .publico(evento.isPublico())
                .organizadorId(evento.getOrganizador().getId())
                .nombreOrganizador(evento.getOrganizador().getNombre())
                .urlImagen(evento.getUrlImagen())
                .build();
    }

    /**
     * Convierte un DTO recibido desde el frontend en una entidad Evento nueva.
     *
     * @param dto El DTO con los datos del evento.
     * @param organizador El usuario que organiza el evento.
     * @return Una nueva entidad Evento lista para persistir.
     */
    public static Evento fromDTO(EventoDTO dto, Usuario organizador) {
        return Evento.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fecha(LocalDateTime.parse(dto.getFecha(), FORMATTER)) // ⏱ String -> LocalDateTime
                .lugar(dto.getLugar())
                .publico(dto.isPublico())
                .urlImagen(dto.getUrlImagen())
                .organizador(organizador)
                .build();
    }

    /**
     * Aplica los cambios de un DTO a una entidad existente.
     * Se usa para operaciones de edición (PUT).
     *
     * @param evento La entidad que ya existe en la base de datos.
     * @param dto    Los nuevos datos que vienen del frontend.
     */
    public static void actualizarDesdeDTO(Evento evento, EventoDTO dto) {
        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setFecha(LocalDateTime.parse(dto.getFecha(), FORMATTER)); // ⏱ String -> LocalDateTime
        evento.setLugar(dto.getLugar());
        evento.setPublico(dto.isPublico());
        evento.setUrlImagen(dto.getUrlImagen());
    }
}
