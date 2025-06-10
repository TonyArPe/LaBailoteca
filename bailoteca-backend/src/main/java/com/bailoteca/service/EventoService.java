package com.bailoteca.service;

import com.bailoteca.dto.EventoDTO;
import com.bailoteca.mapper.EventoMapper;
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

/**
 * Servicio que gestiona la lógica de negocio relacionada con los eventos.
 * Controla los permisos de acceso y transformación entre entidades y DTOs.
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
        List<Evento> eventos = switch (usuario.getRol()) {
            case ADMIN -> eventoRepo.findAll();
            case PROFESOR -> eventoRepo.findByOrganizadorId(usuario.getId());
            case USUARIO -> {
                List<Long> profesorIds = usuario.getInscripciones().stream()
                        .filter(i -> i.getClase() != null && i.getClase().getProfesor() != null)
                        .map(i -> i.getClase().getProfesor().getId())
                        .distinct()
                        .toList();
                yield eventoRepo.findAll().stream()
                        .filter(e -> profesorIds.contains(e.getOrganizador().getId()) || e.isPublico())
                        .toList();
            }
            default -> List.of();
        };
        return eventos.stream().map(EventoMapper::toDTO).toList();
    }

    /**
     * Obtiene un evento por ID si el usuario tiene permiso para verlo.
     */
    public Optional<Evento> obtenerEventoPorIdYUsuario(Long id, Usuario usuario) {
        Optional<Evento> eventoOpt = eventoRepo.findById(id);
        if (eventoOpt.isEmpty()) return Optional.empty();

        Evento evento = eventoOpt.get();
        boolean autorizado = usuario.getRol().name().equals("ADMIN") ||
                (evento.getOrganizador() != null && evento.getOrganizador().getId().equals(usuario.getId()));

        return autorizado ? Optional.of(evento) : Optional.empty();
    }

    /**
     * Crea un nuevo evento con el organizador indicado.
     */
    public Evento crearEvento(Evento evento, Usuario organizador) {
        evento.setOrganizador(organizador);
        evento.setEstado(EstadoEvento.ACTIVO);
        log.info("Evento '{}' creado por {}", evento.getNombre(), organizador.getCorreo());
        return eventoRepo.save(evento);
    }

    /**
     * Actualiza un evento si el usuario tiene permisos.
     */
    @Transactional
    public Evento actualizarEvento(Long id, Evento nuevosDatos, Usuario actual) {
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

        return evento;
    }

    /**
     * Elimina un evento si el usuario tiene permisos.
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
    public List<Evento> obtenerPublicos() {
        return eventoRepo.findByEstado(EstadoEvento.ACTIVO).stream()
                .filter(Evento::isPublico)
                .toList();
    }

    public Evento obtenerPorId(Long id) {
        return eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    public List<Evento> obtenerPorOrganizador(Long id) {
        return eventoRepo.findByOrganizadorId(id);
    }
}