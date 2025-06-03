package com.bailoteca.controller.evento;

import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador REST para gestionar los eventos dentro de Bailoteca.
 * Permite a administradores y profesores crear, modificar y eliminar eventos.
 * Los usuarios pueden consultar los eventos en los que participan.
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;
    private final UsuarioRepo usuarioRepo;

    /**
     * Devuelve todos los eventos si el usuario es administrador o profesor.
     * Si es usuario normal, solo los eventos donde es organizador.
     */
    @GetMapping
    public List<Evento> getAll(Authentication auth) {
        Usuario u = getUsuario(auth);
        if (u.getRol().name().equals("ADMIN") || u.getRol().name().equals("PROFESOR")) {
            return eventoService.obtenerTodos();
        }
        return eventoService.obtenerPorOrganizador(u.getId());
    }

    /**
     * Devuelve los detalles de un evento por ID.
     */
    @GetMapping("/{id}")
    public Evento getOne(@PathVariable Long id) {
        return eventoService.obtenerPorId(id);
    }

    /**
     * Crea un evento si el usuario tiene permisos.
     * Solo pueden crear eventos administradores o profesores.
     */
    @PostMapping
    public Evento create(@RequestBody Evento evento, Authentication auth) {
        Usuario u = getUsuario(auth);
        return eventoService.obtenerEventosAutenticado(u);
        
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear eventos");
    }

    /**
     * Actualiza un evento si el usuario es el organizador o admin.
     */
    @PutMapping("/{id}")
    public Evento update(@PathVariable Long id, @RequestBody Evento datos, Authentication auth) {
        Usuario u = getUsuario(auth);
        return eventoService.actualizarEvento(id, datos, u);
    }

    /**
     * Elimina un evento si el usuario es el organizador o admin.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Usuario u = getUsuario(auth);
        eventoService.eliminarEvento(id, u);
    }

    /**
     * Devuelve la lista de eventos públicos activos visibles para invitados.
     */
    @GetMapping("/publicos")
    public List<Evento> getPublicos() {
        return eventoService.obtenerPublicos();
    }

    /**
     * Extrae el usuario autenticado desde el contexto de seguridad JWT.
     */
    private Usuario getUsuario(Authentication auth) {
        return usuarioRepo.findByCorreo(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}