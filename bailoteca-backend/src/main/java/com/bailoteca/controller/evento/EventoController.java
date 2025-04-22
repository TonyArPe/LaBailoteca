package com.bailoteca.controller.evento;

import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.evento.EstadoEvento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.evento.EventoRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar los eventos
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoRepo eventoRepo;
    private final UsuarioRepo usuarioRepo;

    /**
     * Obtiene todos los eventos registrados.
     */
    @GetMapping
    public List<Evento> getEventos() {
        return eventoRepo.findAll();
    }

    /**
     * Obtiene un evento por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Evento> getEvento(@PathVariable Long id) {
        return ResponseEntity.of(eventoRepo.findById(id));
    }

    /**
     * Crea un nuevo evento si el usuario es ADMIN o PROFESOR
     */
    @PostMapping
    public ResponseEntity<Evento> createEvento(@RequestBody Evento evento) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (actual.getRol().name().equals("ADMIN") || actual.getRol().name().equals("PROFESOR")) {
            evento.setOrganizador(actual);
            evento.setEstado(EstadoEvento.ACTIVO);
            return ResponseEntity.ok(eventoRepo.save(evento));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Actualiza un evento si el usuario es el organizador o un ADMIN
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvento(@PathVariable Long id, @RequestBody Evento datosEvento) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return eventoRepo.findById(id).map(evento -> {
            if (actual.getRol().name().equals("ADMIN") ||
                    (evento.getOrganizador() != null && evento.getOrganizador().getId().equals(actual.getId()))) {

                evento.setNombre(datosEvento.getNombre());
                evento.setDescripcion(datosEvento.getDescripcion());
                evento.setFecha(datosEvento.getFecha());
                evento.setLugar(datosEvento.getLugar());
                evento.setEstado(datosEvento.getEstado());

                return ResponseEntity.ok(eventoRepo.save(evento));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un evento si el usuario es el organizador o un ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvento(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return eventoRepo.findById(id).map(evento -> {
            if (actual.getRol().name().equals("ADMIN") ||
                    (evento.getOrganizador() != null && evento.getOrganizador().getId().equals(actual.getId()))) {
                eventoRepo.deleteById(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene el usuario autenticado desde el JWT
     */
    private Usuario getUsuarioAutenticado() {
        try {
            String correo = ((UserDetails) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal()).getUsername();
            return usuarioRepo.findByCorreo(correo).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}