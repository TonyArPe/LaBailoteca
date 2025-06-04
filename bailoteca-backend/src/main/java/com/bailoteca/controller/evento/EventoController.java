package com.bailoteca.controller.evento;

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

    @GetMapping
    public List<Evento> getAll(Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Listando todos los eventos visibles para {}", u.getCorreo());
        return eventoService.obtenerEventosAutenticado(u);
    }

    @GetMapping("/{id}")
    public Evento getOne(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Solicitando evento con id {} por {}", id, u.getCorreo());
        return eventoService.obtenerEventoPorIdYUsuario(id, u)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este evento"));
    }

    @PostMapping
    public Evento create(@RequestBody Evento evento, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Creando evento por {}", u.getCorreo());

        if (u.getRol() != null &&
                !(u.getRol().name().equals("ADMIN") || u.getRol().name().equals("PROFESOR"))) {
            log.warn("Intento de creación de evento no autorizado por {}", u.getCorreo());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear eventos");
        }

        return eventoService.crearEvento(evento, u);
    }

    @PutMapping("/{id}")
    public Evento update(@PathVariable Long id, @RequestBody Evento datos, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Actualizando evento con id {} por {}", id, u.getCorreo());
        return eventoService.actualizarEvento(id, datos, u);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("Eliminando evento con id {} por {}", id, u.getCorreo());
        eventoService.eliminarEvento(id, u);
    }

    @GetMapping("/publicos")
    public List<Evento> getPublicos() {
        log.info("Listando eventos públicos");
        return eventoService.obtenerPublicos();
    }

    private Usuario getUsuario(Authentication auth) {
        return usuarioRepo.findByCorreo(auth.getName())
                .orElseThrow(() -> {
                    log.error("Usuario con correo {} no encontrado", auth.getName());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
    }
}