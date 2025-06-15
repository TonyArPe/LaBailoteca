package com.bailoteca.controller.evento;

import com.bailoteca.dto.EventoDTO;
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
 * Controlador REST para gestionar los eventos de la aplicación.
 * Soporta operaciones CRUD según el rol del usuario autenticado.
 * También permite acceder a los eventos públicos sin autenticación.
 * 
 * @author
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
public class EventoController {

    private final EventoService eventoService;
    private final UsuarioRepo usuarioRepo;

    /**
     * Devuelve una lista de eventos visibles para el usuario autenticado.
     *
     * @param auth Autenticación del usuario
     * @return Lista de eventos accesibles para ese usuario
     */
    @GetMapping
    public List<EventoDTO> getAll(Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("📋 Listando eventos visibles para {}", u.getCorreo());
        return eventoService.obtenerEventosAutenticado(u).stream()
                .map(EventoMapper::toDTO)
                .toList();
    }

    /**
     * Devuelve los detalles de un evento por ID, si el usuario tiene acceso.
     *
     * @param id   ID del evento
     * @param auth Autenticación del usuario
     * @return EventoDTO detallado
     */
    @GetMapping("/{id}")
    public EventoDTO getOne(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("🔍 Solicitando evento con ID {} por {}", id, u.getCorreo());
        Evento evento = eventoService.obtenerEventoPorIdYUsuario(id, u)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este evento"));
        return EventoMapper.toDTO(evento);
    }

    /**
     * Crea un nuevo evento si el usuario tiene permisos adecuados.
     *
     * @param dto  DTO con los datos del evento a crear
     * @param auth Autenticación del usuario
     * @return EventoDTO creado
     */
    @PostMapping
    public EventoDTO create(@RequestBody EventoDTO dto, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("🆕 Creando evento por {}", u.getCorreo());

        if (u.getRol() == null || !(u.getRol().name().equals("ADMIN") || u.getRol().name().equals("PROFESOR"))) {
            log.warn("⛔ Intento de creación de evento no autorizado por {}", u.getCorreo());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear eventos");
        }

        Evento nuevo = EventoMapper.fromDTO(dto, u);
        Evento guardado = eventoService.crearEvento(nuevo, u);
        return EventoMapper.toDTO(guardado);
    }

    /**
     * Actualiza un evento existente si el usuario tiene permisos adecuados.
     *
     * @param id   ID del evento a actualizar
     * @param dto  DTO con los nuevos datos
     * @param auth Autenticación del usuario
     * @return EventoDTO actualizado
     */
    @PutMapping("/{id}")
    public EventoDTO update(@PathVariable Long id, @RequestBody EventoDTO dto, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("✏️ Actualizando evento ID {} por {}", id, u.getCorreo());
        Evento eventoExistente = eventoService.obtenerEventoPorIdYUsuario(id, u)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este evento"));

        EventoMapper.actualizarDesdeDTO(eventoExistente, dto);
        Evento actualizado = eventoService.actualizarEvento(id, eventoExistente, u);
        return EventoMapper.toDTO(actualizado);
    }

    /**
     * Elimina un evento por su ID si el usuario tiene permisos.
     *
     * @param id   ID del evento
     * @param auth Autenticación del usuario
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("🗑️ Eliminando evento ID {} por {}", id, u.getCorreo());
        eventoService.eliminarEvento(id, u);
    }

    /**
     * Devuelve todos los eventos públicos visibles para invitados (sin login).
     *
     * @return Lista de eventos públicos
     */
    @GetMapping("/publicos")
    public List<EventoDTO> getPublicos() {
        log.info("🌍 Listando eventos públicos (modo invitado)");
        return eventoService.obtenerPublicos().stream()
                .map(EventoMapper::toDTO)
                .toList();
    }

    /**
     * Obtiene el usuario autenticado actual desde el contexto de seguridad.
     *
     * @param auth Objeto de autenticación
     * @return Usuario autenticado
     */
    private Usuario getUsuario(Authentication auth) {
        return usuarioRepo.findByCorreo(auth.getName())
                .orElseThrow(() -> {
                    log.error("❌ Usuario con correo {} no encontrado", auth.getName());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
                });
    }
}