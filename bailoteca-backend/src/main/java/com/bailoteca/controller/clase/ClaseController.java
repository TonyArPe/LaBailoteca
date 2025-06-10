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
 * Controlador que maneja las operaciones relacionadas con las clases.
 * Permite crear, actualizar, eliminar y consultar clases, así como obtener
 * las clases públicas visibles para invitados.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Clase
 * @see ClaseRequest
 * @see ClaseService
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

    /**
     * Obtiene todas las clases disponibles.
     * 
     * @return Lista de todas las clases.
     */
    @GetMapping
    public List<Clase> getAll() {
        return claseRepo.findAll();
    }

    /**
     * Obtiene una clase por su ID.
     * 
     * @param id ID de la clase a buscar.
     * @return Clase encontrada o 404 Not Found si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Clase> getById(@PathVariable Long id) {
        return ResponseEntity.of(claseRepo.findById(id));
    }

    /**
     * Obtiene todas las clases públicas visibles para invitados.
     * 
     * @return Lista de clases públicas.
     */
    @GetMapping("/publicas")
    public List<Clase> obtenerClasesPublicas() {
        return claseService.obtenerTodasLasClasesVisiblesParaInvitados();
    }

    /**
     * Obtiene las clases propias del usuario autenticado.
     * 
     * @return Lista de clases del profesor autenticado o 401 Unauthorized si no hay sesión.
     */
    @PreAuthorize("hasRole('PROFESOR')")
    @GetMapping("/mias")
    public ResponseEntity<List<Clase>> getClasesPropias() {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(claseRepo.findByProfesorId(actual.getId()));
    }

    /**
     * Busca clases por nombre, ignorando mayúsculas y minúsculas.
     * 
     * @param nombre Nombre o parte del nombre de la clase a buscar.
     * @return Lista de clases que coinciden con el nombre proporcionado.
     */
    @GetMapping("/buscar")
    public List<Clase> buscarPorNombre(@RequestParam String nombre) {
        return claseRepo.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Obtiene las clases asociadas a un profesor específico.
     * 
     * @param profesorId ID del profesor cuyas clases se desean obtener.
     * @return Lista de clases del profesor o 404 Not Found si no existe el profesor.
     */
    @GetMapping("/profesor/{profesorId}")
    public List<Clase> getByProfesor(@PathVariable Long profesorId) {
        return claseRepo.findByProfesorId(profesorId);
    }

    /**
     * Crea una nueva clase asignada al usuario autenticado o a un profesor específico si es ADMIN.
     * 
     * @param claseRequest Datos de la clase a crear.
     * @return Clase creada con estado 201 Created, o 401 Unauthorized si no hay sesión.
     */
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

    /**
     * Actualiza una clase existente si el usuario autenticado es su propietario o ADMIN.
     * 
     * @param id ID de la clase a actualizar
     * @param claseRequest Datos actualizados de la clase
     * @return Clase actualizada con estado 200 OK, o 403 Forbidden si no tiene permisos,
     *         404 Not Found si no existe
     */
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
     * @return 204 No Content si se eliminó correctamente, o 403 Forbidden si no tiene permisos,
     *         404 Not Found si no existe
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