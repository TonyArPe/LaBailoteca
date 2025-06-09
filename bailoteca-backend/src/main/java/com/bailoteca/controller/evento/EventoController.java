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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador REST para gestionar eventos dentro del sistema Bailoteca.
 * Incluye funcionalidades para consultar, crear, actualizar y eliminar eventos,
 * así como obtener eventos públicos o asistidos por el usuario autenticado.
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
public class EventoController {
    
    private final EventoService eventoService;
    private final UsuarioRepo usuarioRepo;

    @GetMapping
    public ResponseEntity<List<EventoDTO>> getAll(Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("📥 Listando eventos visibles para el usuario: {}", u.getCorreo());
        return ResponseEntity.ok(eventoService.obtenerEventosAutenticado(u));
    }

    @PostMapping
    public ResponseEntity<EventoDTO> create(@RequestBody EventoRequest request, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("📝 Creación de evento por {}", u.getCorreo());

        if (u.getRol() != null && !(u.getRol().name().equals("ADMIN") || u.getRol().name().equals("PROFESOR"))) {
            log.warn("🚫 Intento de creación de evento no autorizado por {}", u.getCorreo());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear eventos");
        }

        Evento evento = eventoService.crearEventoDesdeRequest(request, u);
        return new ResponseEntity<>(EventoDTO.from(evento), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoDTO> update(@PathVariable Long id, @RequestBody EventoRequest datos, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("✏️ Actualización del evento {} por {}", id, u.getCorreo());
        Evento evento = eventoService.actualizarEventoDesdeRequest(id, datos, u);
        return ResponseEntity.ok(EventoDTO.from(evento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        log.info("🗑️ Eliminando evento {} por {}", id, u.getCorreo());
        eventoService.eliminarEvento(id, u);
        return ResponseEntity.noContent().build();
    }

    private Usuario getUsuario(Authentication auth) {
        return usuarioRepo.findByCorreo(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}