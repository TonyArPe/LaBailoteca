package com.bailoteca.controller.evento;

import com.bailoteca.dto.EventoDTO;
import com.bailoteca.dto.EventoRequest;
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
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
public class EventoController {

    private final EventoService eventoService;
    private final UsuarioRepo usuarioRepo;

    /**
     * Devuelve los eventos visibles para el usuario autenticado según su rol.
     */
    @GetMapping
    public List<EventoDTO> getAll(Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Listando todos los eventos visibles para {}", u.getCorreo());
        return eventoService.obtenerEventosAutenticado(u);
    }

    /**
     * Devuelve un evento específico si el usuario tiene permiso de acceso.
     */
    @GetMapping("/{id}")
    public EventoDTO getOne(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Solicitando evento con id {} por {}", id, u.getCorreo());
        return eventoService.obtenerEventoPorIdYUsuario(id, u)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este evento"));
    }

    /**
     * Permite a un admin o profesor crear un nuevo evento desde un DTO.
     */
    @PostMapping
    public Evento create(@RequestBody EventoRequest request, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Creando evento por {}", u.getCorreo());

        if (u.getRol() != null && !(u.getRol().name().equals("ADMIN") || u.getRol().name().equals("PROFESOR"))) {
            log.warn("Intento de creación de evento no autorizado por {}", u.getCorreo());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear eventos");
        }

        return eventoService.crearEventoDesdeRequest(request, u);
    }

    /**
     * Actualiza un evento existente si el usuario tiene permisos (admin o creador).
     */
    @PutMapping("/{id}")
    public Evento update(@PathVariable Long id, @RequestBody EventoRequest datos, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Actualizando evento con id {} por {}", id, u.getCorreo());
        return eventoService.actualizarEventoDesdeRequest(id, datos, u);
    }

    /**
     * Elimina un evento si el usuario es admin o el organizador.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Eliminando evento con id {} por {}", id, u.getCorreo());
        eventoService.eliminarEvento(id, u);
    }

    /**
     * Devuelve los eventos públicos disponibles para cualquier usuario o invitado.
     */
    @GetMapping("/publicos")
    public List<EventoDTO> getPublicos() {
        log.info("Listando eventos públicos");
        return eventoService.obtenerPublicos();
    }

    /**
     * Recupera el usuario autenticado a partir del objeto Authentication.
     */
    private Usuario getUsuario(Authentication auth) {
        return usuarioRepo.findByCorreo(auth.getName())
                .orElseThrow(() -> {
                    log.error("Usuario con correo {} no encontrado", auth.getName());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
    }
}