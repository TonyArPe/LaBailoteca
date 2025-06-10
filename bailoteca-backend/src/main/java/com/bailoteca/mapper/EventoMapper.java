package com.bailoteca.mapper;

import com.bailoteca.dto.EventoDTO;
import com.bailoteca.dto.EventoRequest;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;

/**
 * Clase utilitaria para convertir entre entidades Evento y DTOs.
 * Esta clase proporciona métodos estáticos para mapear
 * Evento a EventoDTO y EventoRequest a Evento.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Evento
 * @see EventoDTO
 * @see EventoRequest
 * @see Usuario
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
     * Convierte un EventoRequest en Evento, asignando el organizador.
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