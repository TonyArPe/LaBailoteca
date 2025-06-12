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
 * Controlador que maneja las operaciones relacionadas con las inscripciones a clases.
 * Permite crear, consultar y eliminar inscripciones, así como obtener las inscripciones
 * de un usuario o de una clase específica.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Inscripcion
 * @see InscripcionRequest
 */
@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionRepo inscripcionRepo;
    private final ClaseRepo claseRepo;
    private final UsuarioRepo usuarioRepo;
    /**
     * Obtiene el usuario autenticado del contexto de seguridad.
     * Si no hay usuario autenticado, devuelve null.
     *
     * @return Usuario autenticado o null si no hay sesión válida.
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
     * Obtiene las inscripciones de un usuario específico.
     * Solo puede acceder el ADMIN, el mismo usuario o un profesor que tenga clases con ese usuario inscrito.
     *
     * @param usuarioId ID del usuario cuyas inscripciones se desean consultar
     * @return Lista de inscripciones del usuario o error 403 si no tiene permisos
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
     * Obtiene las inscripciones de una clase específica.
     * Solo puede acceder el ADMIN o el profesor dueño de la clase.
     *
     * @param claseId ID de la clase cuyas inscripciones se desean consultar
     * @return Lista de inscripciones de la clase o error 403 si no tiene permisos
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
     * Crea una nueva inscripción a una clase.
     * Solo puede acceder el usuario autenticado y debe ser el mismo que el indicado en la solicitud.
     * Verifica que la clase exista y que el usuario no esté ya inscrito.
     *
     * @param request Datos de la inscripción a crear
     * @return Inscripción creada o error 401, 404 o 409 según corresponda
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
     * Elimina una inscripción por su ID.
     * Solo puede acceder el usuario autenticado, el profesor de la clase o un ADMIN.
     *
     * @param id ID de la inscripción a eliminar
     * @return Respuesta HTTP 200 OK si se eliminó, 403 Forbidden si no tiene permisos, o 404 Not Found si no existe
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
     * Obtiene las inscripciones del usuario autenticado.
     * Solo puede acceder el usuario autenticado.
     *
     * @return Lista de inscripciones del usuario o error 401 si no está autenticado
     */
    @GetMapping("/mias")
    public ResponseEntity<List<Inscripcion>> getInscripcionesDelAutenticado() {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(inscripcionRepo.findByUsuarioId(actual.getId()));
    }

    /**
     * Obtiene las inscripciones de un profesor específico.
     * Solo puede acceder el ADMIN o el mismo profesor.
     *
     * @param profesorId ID del profesor cuyas inscripciones se desean consultar
     * @return Lista de inscripciones del profesor o error 403 si no tiene permisos
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
     * Obtiene el usuario autenticado del contexto de seguridad.
     * Si no hay usuario autenticado, devuelve null.
     *
     * @return Usuario autenticado o null si no hay sesión válida.
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