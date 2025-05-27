package com.bailoteca.controller.clase;

import com.bailoteca.dto.ClaseRequest;
import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.clase.ClaseRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.service.ClaseService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar Clases.
 */
@RestController
@RequestMapping("/api/clases")
@RequiredArgsConstructor
public class ClaseController {

    private final ClaseRepo claseRepo;
    private final UsuarioRepo usuarioRepo;
    private final ClaseService claseService;

    /**
     * Devuelve todas las clases disponibles (acceso público o autenticado).
     */
    @GetMapping
    public List<Clase> getAll() {
        return claseRepo.findAll();
    }

    /**
     * Devuelve una clase por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Clase> getById(@PathVariable Long id) {
        return ResponseEntity.of(claseRepo.findById(id));
    }

    /**
     * Buscar clases por nombre.
     */
    @GetMapping("/buscar")
    public List<Clase> buscarPorNombre(@RequestParam String nombre) {
        return claseRepo.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Devuelve clases por ID del profesor.
     */
    @GetMapping("/profesor/{profesorId}")
    public List<Clase> getByProfesor(@PathVariable Long profesorId) {
        return claseRepo.findByProfesorId(profesorId);
    }

    /**
     * Crea una nueva clase (solo ADMIN o PROFESOR).
     */
    @PostMapping
    public ResponseEntity<Clase> createClase(@RequestBody ClaseRequest claseRequest) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (actual.getRol().name().equals("ADMIN") || actual.getRol().name().equals("PROFESOR")) {
            Clase clase = new Clase();
            clase.setNombre(claseRequest.getNombre());
            clase.setDescripcion(claseRequest.getDescripcion());
            clase.setVideoPresentacion(claseRequest.getVideoPresentacion());
            clase.setDificultad(claseRequest.getDificultad());
            clase.setPublica(claseRequest.isPublica());
            clase.setProfesor(actual);

            return ResponseEntity.ok(claseRepo.save(clase));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Actualiza una clase si el usuario autenticado es el profesor asignado o
     * ADMIN.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Clase> updateClase(@PathVariable Long id, @RequestBody ClaseRequest claseRequest) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return claseRepo.findById(id).map(clase -> {
            if (actual.getRol().name().equals("ADMIN") || clase.getProfesor().getId().equals(actual.getId())) {
                clase.setNombre(claseRequest.getNombre());
                clase.setDescripcion(claseRequest.getDescripcion());
                clase.setVideoPresentacion(claseRequest.getVideoPresentacion());
                clase.setDificultad(claseRequest.getDificultad());
                clase.setPublica(claseRequest.isPublica());
                return ResponseEntity.ok(claseRepo.save(clase));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).<Clase>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina una clase si el usuario autenticado es el profesor asignado o ADMIN.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClase(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return claseRepo.findById(id).map(clase -> {
            if (actual.getRol().name().equals("ADMIN") || clase.getProfesor().getId().equals(actual.getId())) {
                claseRepo.deleteById(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }).orElse(ResponseEntity.notFound().build());
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

    /**
     * Devuelve todas las clases visibles públicamente para usuarios invitados.
     */
    @GetMapping("/publicas")
    public List<Clase> obtenerClasesPublicas() {
        return claseService.obtenerTodasLasClasesVisiblesParaInvitados();
    }

}