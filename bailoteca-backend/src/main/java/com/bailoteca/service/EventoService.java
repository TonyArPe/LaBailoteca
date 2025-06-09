package com.bailoteca.service;

import com.bailoteca.dto.EventoDTO;
import com.bailoteca.dto.EventoRequest;
import com.bailoteca.models.enums.EstadoEvento;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.evento.EventoRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona la lógica de negocio relacionada con los eventos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventoService {

    private final EventoRepo eventoRepo;

    /**
     * Devuelve los eventos visibles para el usuario autenticado según su rol.
     */
    public List<EventoDTO> obtenerEventosAutenticado(Usuario usuario) {
        List<Evento> eventos;
        switch (usuario.getRol()) {
            case ADMIN -> eventos = eventoRepo.findAll();
            case PROFESOR -> eventos = eventoRepo.findByOrganizadorId(usuario.getId());
            case USUARIO -> {
                List<Long> profesorIds = usuario.getInscripciones().stream()
                        .filter(i -> i.getClase() != null && i.getClase().getProfesor() != null)
                        .map(i -> i.getClase().getProfesor().getId())
                        .distinct()
                        .toList();
                eventos = eventoRepo.findAll().stream()
                        .filter(e -> profesorIds.contains(e.getOrganizador().getId()))
                        .toList();
            }
            default -> {
                log.warn("Rol desconocido: {}", usuario.getRol());
                eventos = List.of();
            }
        }
        return eventos.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Devuelve un evento específico si el usuario tiene permiso de acceso.
     */
    public Optional<EventoDTO> obtenerEventoPorIdYUsuario(Long id, Usuario usuario) {
        Optional<Evento> eventoOpt = eventoRepo.findById(id);
        if (eventoOpt.isEmpty()) return Optional.empty();

        Evento evento = eventoOpt.get();
        boolean autorizado = usuario.getRol().name().equals("ADMIN") ||
                (evento.getOrganizador() != null && evento.getOrganizador().getId().equals(usuario.getId()));

        return autorizado ? Optional.of(mapToDto(evento)) : Optional.empty();
    }

    /**
     * Crea un nuevo evento a partir de un EventoRequest.
     */
    public Evento crearEventoDesdeRequest(EventoRequest request, Usuario organizador) {
        Evento evento = new Evento();
        evento.setNombre(request.getNombre());
        evento.setDescripcion(request.getDescripcion());
        evento.setFecha(request.getFecha());
        evento.setLugar(request.getLugar());
        evento.setEstado(request.getEstado());
        evento.setPublico(request.isPublico());
        evento.setOrganizador(organizador);
        log.info("Evento '{}' creado por {}", evento.getNombre(), organizador.getCorreo());
        return eventoRepo.save(evento);
    }

    /**
     * Actualiza un evento existente si el usuario tiene permisos.
     */
    @Transactional
    public Evento actualizarEventoDesdeRequest(Long id, EventoRequest nuevosDatos, Usuario actual) {
        Evento evento = eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        boolean autorizado = actual.getRol().name().equals("ADMIN") ||
                (evento.getOrganizador() != null && evento.getOrganizador().getId().equals(actual.getId()));

        if (!autorizado) {
            log.warn("Usuario {} sin permiso para actualizar evento {}", actual.getCorreo(), id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para editar este evento");
        }

        evento.setNombre(nuevosDatos.getNombre());
        evento.setDescripcion(nuevosDatos.getDescripcion());
        evento.setFecha(nuevosDatos.getFecha());
        evento.setLugar(nuevosDatos.getLugar());
        evento.setEstado(nuevosDatos.getEstado());
        evento.setPublico(nuevosDatos.isPublico());

        log.info("Evento con id {} actualizado por {}", id, actual.getCorreo());
        return evento;
    }

    /**
     * Elimina un evento si el usuario es admin o su organizador.
     */
    public void eliminarEvento(Long id, Usuario actual) {
        Evento evento = eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        boolean autorizado = actual.getRol().name().equals("ADMIN") ||
                (evento.getOrganizador() != null && evento.getOrganizador().getId().equals(actual.getId()));

        if (!autorizado) {
            log.warn("Usuario {} sin permiso para eliminar evento {}", actual.getCorreo(), id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar este evento");
        }

        eventoRepo.delete(evento);
        log.info("Evento con id {} eliminado por {}", id, actual.getCorreo());
    }

    /**
     * Devuelve todos los eventos públicos activos.
     */
    public List<EventoDTO> obtenerPublicos() {
        return eventoRepo.findByEstado(EstadoEvento.ACTIVO).stream()
                .filter(Evento::isPublico)
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Convierte un Evento a su representación DTO para exponer al frontend.
     */
    public EventoDTO mapToDto(Evento evento) {
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