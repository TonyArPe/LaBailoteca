package com.bailoteca.controller.inscripcion;

import com.bailoteca.exceptions.OperacionNoPermitidaException;
import com.bailoteca.models.inscripcion.Inscripcion;
import com.bailoteca.repository.inscripcion.InscripcionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionRepo inscripcionRepo;

    /**
     * Obtiene todas las inscripciones.
     */
    @GetMapping
    public List<Inscripcion> getAll() {
        return inscripcionRepo.findAll();
    }

    /**
     * Obtener inscripciones de un usuario específico.
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<Inscripcion> getByUsuario(@PathVariable Long usuarioId) {
        return inscripcionRepo.findByUsuarioId(usuarioId);
    }

    /**
     * Obtener inscripciones por clase.
     */
    @GetMapping("/clase/{claseId}")
    public List<Inscripcion> getByClase(@PathVariable Long claseId) {
        return inscripcionRepo.findByClaseId(claseId);
    }

    /**
     * Inscribir un usuario a una clase.
     * Se comprueba tambien si el usuario ya está inscrito en la clase.
     */
    @PostMapping
    public Inscripcion create(@RequestBody Inscripcion inscripcion) {
        boolean yaInscrito = inscripcionRepo.existsByUsuarioIdAndClaseId(
                inscripcion.getUsuario().getId(),
                inscripcion.getClase().getId());

        if (yaInscrito) {
            throw new OperacionNoPermitidaException("El usuario ya está inscrito en esta clase.");
        }

        return inscripcionRepo.save(inscripcion);
    }

    /**
     * Eliminar una inscripción.
     */
    // @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')") // <- cuando activemos roles
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        inscripcionRepo.deleteById(id);
    }

}
