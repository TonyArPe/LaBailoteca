package com.bailoteca.service.evento;

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
 * Servicio encargado de la gestión de eventos en el sistema Bailoteca.
 * Permite crear, actualizar, eliminar y consultar eventos según el rol del usuario autenticado.
 * Este servicio maneja las operaciones CRUD y las restricciones de acceso basadas en roles.
 *
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Evento
 * @see EventoDTO
 * @see EventoMapper
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventoService {

    private final EventoRepo eventoRepo;

    /**
     * Obtiene todos los eventos visibles para el usuario autenticado según su rol.
     * - ADMIN: ve todos los eventos.
     * - PROFESOR: ve solo sus eventos.
     * - USUARIO: ve eventos de profesores a los que está inscrito o eventos públicos.
     *
     * @param usuario Usuario autenticado
     * @return Lista de eventos visibles para el usuario
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
     * Obtiene un evento por ID, verificando si el usuario tiene permisos para verlo.
     * - ADMIN: acceso total.
     * - PROFESOR: acceso a sus propios eventos.
     * - USUARIO: acceso a eventos públicos o de profesores a los que está inscrito.
     *
     * @param id      ID del evento
     * @param usuario Usuario autenticado
     * @return Evento si el usuario tiene acceso, vacío en caso contrario
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
     * Crea un nuevo evento asignando el organizador y estableciendo el estado inicial.
     * El evento se guarda en la base de datos.
     *
     * @param evento      Evento a crear
     * @param organizador Usuario que organiza el evento
     * @return Evento creado
     */
    public Evento crearEvento(Evento evento, Usuario organizador) {
        evento.setOrganizador(organizador);
        evento.setEstado(EstadoEvento.ACTIVO);
        log.info("Evento '{}' creado por {}", evento.getNombre(), organizador.getCorreo());
        return eventoRepo.save(evento);
    }

    /**
     * Actualiza un evento existente si el usuario tiene permisos.
     * - ADMIN: puede actualizar cualquier evento.
     * - PROFESOR: puede actualizar solo sus propios eventos.
     *
     * @param id          ID del evento a actualizar
     * @param nuevosDatos Nuevos datos del evento
     * @param actual      Usuario que realiza la actualización
     * @return Evento actualizado
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
     * - ADMIN: puede eliminar cualquier evento.
     * - PROFESOR: puede eliminar solo sus propios eventos.
     *
     * @param id     ID del evento a eliminar
     * @param actual Usuario que realiza la eliminación
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
     * Obtiene todos los eventos públicos activos.
     * Un evento es público si su campo 'publico' es verdadero y su estado es 'ACTIVO'.
     *
     * @return Lista de eventos públicos activos
     */
    public List<Evento> obtenerPublicos() {
        return eventoRepo.findByEstado(EstadoEvento.ACTIVO).stream()
                .filter(Evento::isPublico)
                .toList();
    }

    /**
     * Obtiene un evento por su ID.
     * Si no se encuentra, lanza una excepción 404 Not Found.
     *
     * @param id ID del evento a buscar
     * @return Evento encontrado
     */
    public Evento obtenerPorId(Long id) {
        return eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    /**
     * Obtiene todos los eventos organizados por un usuario específico.
     *
     * @param id ID del organizador
     * @return Lista de eventos organizados por el usuario
     */
    public List<Evento> obtenerPorOrganizador(Long id) {
        return eventoRepo.findByOrganizadorId(id);
    }
}