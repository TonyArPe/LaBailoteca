package com.bailoteca.controller.clase;

import com.bailoteca.dto.ClaseRequest;
import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.clase.HorarioClase;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.clase.ClaseRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.security.UsuarioDetails;
import com.bailoteca.service.clase.ClaseService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador REST para la gestión de Clases y Horarios.
 * Sólo profesores pueden gestionar sus propias clases.
 * Admin puede acceder a todas.
 * Este controlador permite crear, actualizar, eliminar y consultar clases,
 * así como obtener clases públicas visibles para invitados.
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Clase
 * @see ClaseRequest
 * @see ClaseService
 * @see ClaseRepo
 * @see Usuario
 * @see UsuarioRepo
 * @see UsuarioDetails
 * @see HorarioClase
 * 
 */
@RestController
@RequestMapping("/api/clases")
@RequiredArgsConstructor
public class ClaseController {

    private final ClaseRepo claseRepo;
    private final ClaseService claseService;
    private final UsuarioRepo usuarioRepo;

    /**
     * Obtiene el usuario autenticado extraído del token JWT de Firebase.
     * 
     * @return El usuario autenticado, o null si no hay sesión válida.
     */
    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails)) {
            System.err.println("⚠️ No se encontró usuario autenticado en el contexto de seguridad.");
            return null;
        }
        Usuario usuario = ((UsuarioDetails) authentication.getPrincipal()).getUsuario();
        System.err.printf("🔐 Usuario autenticado: {} (ID: {})", usuario.getCorreo(), usuario.getId());
        return usuario;
    }

    @GetMapping
    public List<Clase> getAll() {
        return claseRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clase> getById(@PathVariable Long id) {
        return ResponseEntity.of(claseRepo.findById(id));
    }

    @GetMapping("/publicas")
    public List<Clase> obtenerClasesPublicas() {
        return claseService.obtenerTodasLasClasesVisiblesParaInvitados();
    }

    @PreAuthorize("hasRole('PROFESOR')")
    @GetMapping("/mias")
    public ResponseEntity<List<Clase>> getClasesPropias() {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(claseRepo.findByProfesorId(actual.getId()));
    }

    @GetMapping("/buscar")
    public List<Clase> buscarPorNombre(@RequestParam String nombre) {
        return claseRepo.findByNombreContainingIgnoreCase(nombre);
    }

    @GetMapping("/profesor/{profesorId}")
    public List<Clase> getByProfesor(@PathVariable Long profesorId) {
        return claseRepo.findByProfesorId(profesorId);
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMIN')")
    public ResponseEntity<Clase> createClase(@RequestBody ClaseRequest claseRequest) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Usuario profesorAsignado = actual;
        if (actual.getRol().name().equals("ADMIN") && claseRequest.getProfesorId() != null) {
            profesorAsignado = usuarioRepo.findById(claseRequest.getProfesorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profesor no encontrado"));
        }

        Clase clase = new Clase();
        clase.setNombre(claseRequest.getNombre());
        clase.setDescripcion(claseRequest.getDescripcion());
        clase.setUbicacion(claseRequest.getUbicacion());
        clase.setVideoPresentacion(claseRequest.getVideoPresentacion());
        clase.setDificultad(claseRequest.getDificultad());
        clase.setPublica(claseRequest.getPublica());
        clase.setProfesor(profesorAsignado);

        List<HorarioClase> horarios = claseRequest.getHorarioClases().stream()
                .map(req -> new HorarioClase(null, req.getDiaSemana(), req.getHoraInicio(), req.getHoraFin(), null))
                .toList();

        Clase guardada = claseService.guardarClaseConHorarios(clase, horarios);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateClase(@PathVariable Long id, @RequestBody ClaseRequest claseRequest) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            System.err.printf("📝 Petición PUT /clases/{} por usuario {}", id, actual.getCorreo());
            Clase actualizada = claseService.actualizarClaseYHorarios(id, claseRequest, actual);
            return ResponseEntity.ok(actualizada);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    /**
     * Elimina una clase si el usuario autenticado es su propietario o ADMIN.
     * 
     * @param id ID de la clase a eliminar
     * @return 204 No Content si se elimina correctamente, 403 si no tiene permisos,
     *         404 si no existe
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMIN')")
    public ResponseEntity<?> eliminarClase(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return claseRepo.findById(id).map(clase -> {
            Long profesorIdClase = clase.getProfesor() != null ? clase.getProfesor().getId() : null;
            boolean esPropietario = profesorIdClase != null && profesorIdClase.equals(actual.getId());
            boolean esAdmin = actual.getRol().name().equals("ADMIN");

            if (esAdmin || esPropietario) {
                claseRepo.delete(clase);
                System.out.printf("🗑️ Clase ID %d eliminada por usuario %s (rol: %s)%n",
                        clase.getId(), actual.getCorreo(), actual.getRol().name());
                return ResponseEntity.noContent().build();
            } else {
                System.err.printf("🚫 Usuario %s intentó eliminar clase ID %d sin permisos%n",
                        actual.getCorreo(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para eliminar esta clase");
            }
        }).orElseGet(() -> {
            System.err.printf("❌ Clase ID %d no encontrada para eliminación%n", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Clase no encontrada");
        });
    }

}