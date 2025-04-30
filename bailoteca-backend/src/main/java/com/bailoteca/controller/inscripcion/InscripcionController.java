package com.bailoteca.controller.inscripcion;

import com.bailoteca.models.inscripcion.Inscripcion;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.dto.InscripcionRequest;
import com.bailoteca.models.enums.EstadoInscripcion;
import com.bailoteca.repository.inscripcion.InscripcionRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.repository.clase.ClaseRepo;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionRepo inscripcionRepo;
    private final UsuarioRepo usuarioRepo;
    private final ClaseRepo claseRepo;

    /**
     * Devuelve todas las inscripciones (solo ADMIN).
     */
    @GetMapping
    public ResponseEntity<List<Inscripcion>> getAll() {
        Usuario actual = getUsuarioAutenticado();
        if (actual != null && actual.getRol().name().equals("ADMIN")) {
            return ResponseEntity.ok(inscripcionRepo.findAll());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Devuelve las inscripciones de un usuario (el mismo o ADMIN).
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Inscripcion>> getByUsuario(@PathVariable Long usuarioId) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (actual.getId().equals(usuarioId) || actual.getRol().name().equals("ADMIN")) {
            return ResponseEntity.ok(inscripcionRepo.findByUsuarioId(usuarioId));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Devuelve inscripciones por clase (ADMIN o profesor dueño).
     */
    @GetMapping("/clase/{claseId}")
    public ResponseEntity<List<Inscripcion>> getByClase(@PathVariable Long claseId) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var clase = claseRepo.findById(claseId).orElse(null);
        if (clase == null)
            return ResponseEntity.notFound().build();

        if (actual.getRol().name().equals("ADMIN") || clase.getProfesor().getId().equals(actual.getId())) {
            return ResponseEntity.ok(inscripcionRepo.findByClaseId(claseId));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Inscribe al usuario autenticado en una clase.
     */
    @PostMapping
    public ResponseEntity<Inscripcion> create(@RequestBody InscripcionRequest request) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null || !actual.getId().equals(request.getUsuarioId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!claseRepo.existsById(request.getClaseId())) {
            return ResponseEntity.notFound().build();
        }

        boolean yaInscrito = inscripcionRepo.existsByUsuarioIdAndClaseId(request.getUsuarioId(), request.getClaseId());
        if (yaInscrito) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Inscripcion inscripcion = Inscripcion.builder()
                .usuario(actual)
                .clase(claseRepo.findById(request.getClaseId()).get())
                .fechaInscripcion(LocalDate.now())
                .estado(EstadoInscripcion.ACTIVA)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(inscripcionRepo.save(inscripcion));
    }

    /**
     * Elimina una inscripción si el usuario es el propietario, ADMIN o profesor
     * dueño de la clase.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return inscripcionRepo.findById(id).map(inscripcion -> {
            Long usuarioId = inscripcion.getUsuario().getId();
            Long profesorId = inscripcion.getClase().getProfesor().getId();

            if (actual.getId().equals(usuarioId) ||
                    actual.getId().equals(profesorId) ||
                    actual.getRol().name().equals("ADMIN")) {
                inscripcionRepo.deleteById(id);
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mias")
    public ResponseEntity<List<Inscripcion>> getInscripcionesDelAutenticado() {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(inscripcionRepo.findByUsuarioId(actual.getId()));
    }

    /**
     * Método auxiliar para obtener el usuario autenticado.
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