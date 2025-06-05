package com.bailoteca.controller.inscripcion;

import com.bailoteca.models.inscripcion.Inscripcion;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.dto.InscripcionRequest;
import com.bailoteca.models.enums.EstadoInscripcion;
import com.bailoteca.repository.inscripcion.InscripcionRepo;
import com.bailoteca.repository.clase.ClaseRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.security.UsuarioDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar inscripciones a clases.
 */
@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionRepo inscripcionRepo;
    private final ClaseRepo claseRepo;
    private final UsuarioRepo usuarioRepo;

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
     * Devuelve las inscripciones de un usuario.
     * Puede acceder el propio usuario, un admin, o un profesor si el usuario está inscrito en alguna de sus clases.
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Inscripcion>> getByUsuario(@PathVariable Long usuarioId) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean esAdmin = actual.getRol().name().equals("ADMIN");
        boolean esMismoUsuario = actual.getId().equals(usuarioId);
        boolean esProfesorYAlumnoInscrito = usuarioRepo.estaInscritoEnClaseDeProfesor(usuarioId, actual.getId());

        if (esAdmin || esMismoUsuario || esProfesorYAlumnoInscrito) {
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
        if (actual == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var clase = claseRepo.findById(claseId).orElse(null);
        if (clase == null) return ResponseEntity.notFound().build();

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
     * Elimina una inscripción si el usuario es el propietario, ADMIN o profesor dueño de la clase.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return inscripcionRepo.findById(id).map(inscripcion -> {
            Long usuarioId = inscripcion.getUsuario().getId();
            Long profesorId = inscripcion.getClase().getProfesor().getId();

            if (actual.getId().equals(usuarioId) || actual.getId().equals(profesorId) || actual.getRol().name().equals("ADMIN")) {
                inscripcionRepo.deleteById(id);
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Devuelve las inscripciones del usuario autenticado.
     */
    @GetMapping("/mias")
    public ResponseEntity<List<Inscripcion>> getInscripcionesDelAutenticado() {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(inscripcionRepo.findByUsuarioId(actual.getId()));
    }

    /**
     * Devuelve todas las inscripciones a clases impartidas por un profesor específico.
     * Solo puede acceder el mismo profesor.
     */
    @PreAuthorize("hasRole('PROFESOR')")
    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<List<Inscripcion>> obtenerInscripcionesPorProfesor(@PathVariable Long profesorId) {
        var actual = getUsuarioAutenticado();

        if (actual == null || !actual.getId().equals(profesorId)) {
            System.err.printf("⛔ Acceso denegado. Usuario ID=%s intentó acceder a inscripciones del profesor ID=%s\n",
                    actual != null ? actual.getId() : "null", profesorId);
            return ResponseEntity.status(403).build();
        }

        List<Inscripcion> inscripciones = inscripcionRepo.findByClaseProfesorId(profesorId);
        System.out.printf("📚 Profesor ID=%d consultó inscripciones: %d resultados\n", profesorId, inscripciones.size());
        return ResponseEntity.ok(inscripciones);
    }

    /**
     * Método auxiliar para obtener el usuario autenticado actual del contexto de seguridad.
     */
    private Usuario getUsuarioAutenticado() {
        try {
            UsuarioDetails details = (UsuarioDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return details.getUsuario();
        } catch (Exception e) {
            return null;
        }
    }
}