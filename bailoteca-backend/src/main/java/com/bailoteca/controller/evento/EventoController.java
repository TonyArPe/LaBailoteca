package com.bailoteca.controller.evento;

import com.bailoteca.dto.EventoDTO;
import com.bailoteca.dto.EventoRequest;
import com.bailoteca.mapper.EventoMapper;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.service.evento.EventoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador que maneja las operaciones relacionadas con los eventos.
 * Permite crear, actualizar, eliminar y consultar eventos, así como obtener
 * los eventos públicos visibles para invitados.
 *
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Evento
 * @see EventoDTO
 * @see EventoRequest
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
public class EventoController {

    private final EventoService eventoService;
    private final UsuarioRepo usuarioRepo;

    /**
     * Obtiene el usuario autenticado a partir del contexto de seguridad.
     * Si no se encuentra, lanza una excepción 404.
     *
     * @param auth Autenticación del usuario
     * @return Usuario autenticado
     */
    @GetMapping
    public List<EventoDTO> getAll(Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Listando eventos visibles para {}", u.getCorreo());
        return eventoService.obtenerEventosAutenticado(u);
    }

    /**
     * Obtiene un evento específico por su ID.
     * Solo los usuarios con permisos pueden acceder a eventos privados.
     *
     * @param id   ID del evento
     * @param auth Autenticación del usuario
     * @return EventoDTO con los detalles del evento
     */
    @GetMapping("/{id}")
    public EventoDTO getOne(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Solicitando evento con id {} por {}", id, u.getCorreo());
        Evento evento = eventoService.obtenerEventoPorIdYUsuario(id, u)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este evento"));
        return EventoMapper.toDTO(evento);
    }

    /**
     * Crea un nuevo evento si el usuario tiene permisos.
     * Solo los usuarios con rol ADMIN o PROFESOR pueden crear eventos.
     *
     * @param request Datos del evento a crear
     * @param auth    Autenticación del usuario
     * @return EventoDTO con los detalles del evento creado
     */
    @PostMapping
    public EventoDTO create(@RequestBody EventoRequest request, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Creando evento por {}", u.getCorreo());

        if (u.getRol() == null || !(u.getRol().name().equals("ADMIN") || u.getRol().name().equals("PROFESOR"))) {
            log.warn("Intento de creación de evento no autorizado por {}", u.getCorreo());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear eventos");
        }

        Evento nuevo = EventoMapper.fromRequest(request, u);
        return EventoMapper.toDTO(eventoService.crearEvento(nuevo, u));
    }

    /**
     * Actualiza un evento existente si el usuario tiene permisos.
     * Solo los usuarios con rol ADMIN o PROFESOR pueden actualizar eventos.
     *
     * @param id      ID del evento a actualizar
     * @param request Nuevos datos del evento
     * @param auth    Autenticación del usuario
     * @return EventoDTO con los detalles del evento actualizado
     */
    @PutMapping("/{id}")
    public EventoDTO update(@PathVariable Long id, @RequestBody EventoRequest request, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Actualizando evento con id {} por {}", id, u.getCorreo());
        Evento nuevosDatos = EventoMapper.fromRequest(request, u);
        return EventoMapper.toDTO(eventoService.actualizarEvento(id, nuevosDatos, u));
    }

    /**
     * Elimina un evento por su ID.
     * Solo los usuarios con rol ADMIN o PROFESOR pueden eliminar eventos.
     *
     * @param id   ID del evento a eliminar
     * @param auth Autenticación del usuario
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Eliminando evento con id {} por {}", id, u.getCorreo());
        eventoService.eliminarEvento(id, u);
    }

    /**
     * Obtiene una lista de eventos públicos visibles para invitados.
     * Estos eventos no requieren autenticación para ser consultados.
     *
     * @return Lista de eventos públicos
     */
    @GetMapping("/publicos")
    public List<EventoDTO> getPublicos() {
        log.info("Listando eventos públicos");
        return eventoService.obtenerPublicos().stream()
                .map(EventoMapper::toDTO)
                .toList();
    }

    private Usuario getUsuario(Authentication auth) {
        return usuarioRepo.findByCorreo(auth.getName())
                .orElseThrow(() -> {
                    log.error("Usuario con correo {} no encontrado", auth.getName());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
    }
}