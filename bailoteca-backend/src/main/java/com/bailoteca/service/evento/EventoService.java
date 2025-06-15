package com.bailoteca.service.evento;

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
 * Servicio de negocio encargado de gestionar los eventos en el sistema Bailoteca.
 * Permite realizar operaciones CRUD sobre eventos, aplicando restricciones según el rol del usuario autenticado.
 * <p>
 * - ADMIN puede ver y modificar todos los eventos.
 * - PROFESOR puede ver y modificar solo sus eventos.
 * - USUARIO ve eventos públicos y de profesores con los que tiene inscripción.
 * </p>
 * 
 * @author
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventoService {

    private final EventoRepo eventoRepo;

    /**
     * Obtiene todos los eventos visibles para el usuario autenticado, en función de su rol.
     *
     * @param usuario Usuario autenticado
     * @return Lista de eventos accesibles para el usuario
     */
    public List<Evento> obtenerEventosAutenticado(Usuario usuario) {
        return switch (usuario.getRol()) {
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
            default -> List.of(); // INVITADO o nulo
        };
    }

    /**
     * Obtiene un evento específico por ID, solo si el usuario tiene permiso para verlo.
     *
     * @param id      ID del evento
     * @param usuario Usuario autenticado
     * @return Evento si el usuario tiene acceso, vacío si no tiene permisos
     */
    public Optional<Evento> obtenerEventoPorIdYUsuario(Long id, Usuario usuario) {
        Optional<Evento> eventoOpt = eventoRepo.findById(id);
        if (eventoOpt.isEmpty()) return Optional.empty();

        Evento evento = eventoOpt.get();
        boolean autorizado = switch (usuario.getRol()) {
            case ADMIN -> true;
            case PROFESOR -> evento.getOrganizador().getId().equals(usuario.getId());
            case USUARIO -> evento.isPublico() || usuario.getInscripciones().stream()
                    .anyMatch(i -> i.getClase() != null &&
                                   i.getClase().getProfesor() != null &&
                                   i.getClase().getProfesor().getId().equals(evento.getOrganizador().getId()));
            default -> false;
        };

        return autorizado ? Optional.of(evento) : Optional.empty();
    }

    /**
     * Crea un nuevo evento, asignando el organizador y estado inicial.
     *
     * @param evento      Evento sin persistir
     * @param organizador Usuario organizador autenticado
     * @return Evento guardado en base de datos
     */
    public Evento crearEvento(Evento evento, Usuario organizador) {
        evento.setOrganizador(organizador);
        evento.setEstado(EstadoEvento.ACTIVO); // Estado por defecto
        Evento guardado = eventoRepo.save(evento);
        log.info("✅ Evento '{}' creado por {}", evento.getNombre(), organizador.getCorreo());
        return guardado;
    }

    /**
     * Actualiza un evento existente si el usuario tiene permisos adecuados.
     *
     * @param id          ID del evento a modificar
     * @param nuevosDatos Entidad con los datos actualizados
     * @param actual      Usuario autenticado que ejecuta la acción
     * @return Evento actualizado
     */
    @Transactional
    public Evento actualizarEvento(Long id, Evento nuevosDatos, Usuario actual) {
        Evento evento = eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        boolean autorizado = actual.getRol().name().equals("ADMIN") ||
                (evento.getOrganizador() != null && evento.getOrganizador().getId().equals(actual.getId()));

        if (!autorizado) {
            log.warn("⛔ Usuario {} no tiene permiso para actualizar evento {}", actual.getCorreo(), id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para editar este evento");
        }

        evento.setNombre(nuevosDatos.getNombre());
        evento.setDescripcion(nuevosDatos.getDescripcion());
        evento.setFecha(nuevosDatos.getFecha());
        evento.setLugar(nuevosDatos.getLugar());
        evento.setEstado(nuevosDatos.getEstado());
        evento.setPublico(nuevosDatos.isPublico());
        evento.setUrlImagen(nuevosDatos.getUrlImagen());

        log.info("✏️ Evento '{}' (ID {}) actualizado por {}", evento.getNombre(), id, actual.getCorreo());
        return evento;
    }

    /**
     * Elimina un evento si el usuario tiene permisos adecuados.
     *
     * @param id     ID del evento
     * @param actual Usuario autenticado
     */
    public void eliminarEvento(Long id, Usuario actual) {
        Evento evento = eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        boolean autorizado = actual.getRol().name().equals("ADMIN") ||
                (evento.getOrganizador() != null && evento.getOrganizador().getId().equals(actual.getId()));

        if (!autorizado) {
            log.warn("⛔ Usuario {} no tiene permiso para eliminar evento {}", actual.getCorreo(), id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar este evento");
        }

        eventoRepo.delete(evento);
        log.info("🗑️ Evento con id {} eliminado por {}", id, actual.getCorreo());
    }

    /**
     * Devuelve todos los eventos públicos en estado ACTIVO.
     *
     * @return Lista de eventos públicos
     */
    public List<Evento> obtenerPublicos() {
        return eventoRepo.findByEstado(EstadoEvento.ACTIVO).stream()
                .filter(Evento::isPublico)
                .toList();
    }

    /**
     * Busca un evento por su ID y lanza 404 si no existe.
     *
     * @param id ID del evento
     * @return Evento encontrado
     */
    public Evento obtenerPorId(Long id) {
        return eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    /**
     * Lista todos los eventos organizados por un usuario concreto.
     *
     * @param id ID del organizador
     * @return Lista de eventos organizados por ese usuario
     */
    public List<Evento> obtenerPorOrganizador(Long id) {
        return eventoRepo.findByOrganizadorId(id);
    }
}