package com.bailoteca.controller.evento;

import com.bailoteca.dto.EventoDTO;
import com.bailoteca.dto.EventoRequest;
import com.bailoteca.mapper.EventoMapper;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.service.EventoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador REST para gestionar eventos dentro del sistema Bailoteca.
 * Soporta operaciones CRUD con restricciones de rol:
 * - ADMIN: control total.
 * - PROFESOR: solo sus propios eventos.
 * - USUARIO/INVITADO: acceso solo lectura.
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
public class EventoController {

    private final EventoService eventoService;
    private final UsuarioRepo usuarioRepo;

    /**
     * Obtiene todos los eventos visibles para el usuario autenticado.
     */
    @GetMapping
    public List<EventoDTO> getAll(Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Listando eventos visibles para {}", u.getCorreo());
        return eventoService.obtenerEventosAutenticado(u);
    }

    /**
     * Obtiene un evento por ID, si el usuario tiene acceso.
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
     * Crea un nuevo evento si el usuario es ADMIN o PROFESOR.
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
     * Actualiza un evento si el usuario tiene permisos.
     */
    @PutMapping("/{id}")
    public EventoDTO update(@PathVariable Long id, @RequestBody EventoRequest request, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Actualizando evento con id {} por {}", id, u.getCorreo());
        Evento nuevosDatos = EventoMapper.fromRequest(request, u);
        return EventoMapper.toDTO(eventoService.actualizarEvento(id, nuevosDatos, u));
    }

    /**
     * Elimina un evento si el usuario tiene permisos.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Eliminando evento con id {} por {}", id, u.getCorreo());
        eventoService.eliminarEvento(id, u);
    }

    /**
     * Devuelve la lista de eventos públicos activos.
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