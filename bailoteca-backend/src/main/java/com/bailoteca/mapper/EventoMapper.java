package com.bailoteca.mapper;

import com.bailoteca.dto.EventoDTO;
import com.bailoteca.dto.EventoRequest;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;

/**
 * Mapper para convertir entre modelos de Evento y sus representaciones DTO.
 * Facilita la transformación de datos entre la capa de persistencia y la capa de presentación.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Evento
 * @see EventoDTO
 * @see EventoRequest
 */
public class EventoMapper {

    /**
     * Convierte un Evento en EventoDTO para respuesta API.
     */
    public static EventoDTO toDTO(Evento evento) {
        return EventoDTO.builder()
                .id(evento.getId())
                .nombre(evento.getNombre())
                .descripcion(evento.getDescripcion())
                .fecha(evento.getFecha())
                .lugar(evento.getLugar())
                .estado(evento.getEstado())
                .publico(evento.isPublico())
                .nombreOrganizador(evento.getOrganizador().getNombre())
                .build();
    }

    /**
     * Convierte un EventoRequest en Evento para persistencia.
     * 
     * @param request El objeto EventoRequest que contiene los datos del evento.
     * @param organizador El usuario que organiza el evento.
     * @return Un objeto Evento con los datos del request y el organizador.
     */
    public static Evento fromRequest(EventoRequest request, Usuario organizador) {
        return Evento.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .lugar(request.getLugar())
                .estado(request.getEstado())
                .publico(request.isPublico())
                .organizador(organizador)
                .build();
    }
}