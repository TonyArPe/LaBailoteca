package com.bailoteca.controller.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.bailoteca.dto.UsuarioDTO;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.security.UsuarioDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar usuarios.
 * Administra registros, actualizaciones, consultas y eliminaciones de usuarios.
 */
@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepo usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios del sistema.
     * - ADMIN: ve todos.
     * - PROFESOR: ve solo alumnos de sus clases.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping
    public ResponseEntity<?> getUsuarios() {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (actual.getRol().name().equals("ADMIN")) {
            log.info("👤 ADMIN solicitó la lista de todos los usuarios");
            return ResponseEntity.ok(usuarioRepo.findAll());
        }

        if (actual.getRol().name().equals("PROFESOR")) {
            log.info("👨‍🏫 PROFESOR solicitó la lista de sus alumnos");
            return ResponseEntity.ok(usuarioRepo.findAlumnosPorProfesorId(actual.getId()));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Registra un nuevo usuario (no requiere autenticación).
     */
    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario) {
        log.info("✍️ Registrando nuevo usuario con correo: {}", usuario.getCorreo());

        if (usuarioRepo.findByCorreo(usuario.getCorreo()).isPresent()) {
            log.warn("⚠️ Correo ya existente: {}", usuario.getCorreo());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }

        usuario.setFechaRegistro(LocalDate.now());
        usuario.setActivo(true);
        usuario.setPagado(false);
        usuario.setContrasenna(passwordEncoder.encode(usuario.getContrasenna()));

        Usuario registrado = usuarioRepo.save(usuario);
        log.info("✅ Usuario registrado correctamente: {} (ID: {})", registrado.getCorreo(), registrado.getId());
        return registrado;
    }

    /**
     * Obtiene un usuario por su ID (ADMIN, PROFESOR si tiene inscripción o el
     * propio usuario).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean esAdmin = actual.getRol().name().equals("ADMIN");
        boolean esMismoUsuario = actual.getId().equals(id);

        if (esAdmin || esMismoUsuario) {
            log.info("👁️ Acceso permitido a usuario con ID {}", id);
            return ResponseEntity.of(usuarioRepo.findById(id));
        }

        if (actual.getRol().name().equals("PROFESOR")) {
            boolean inscrito = usuarioRepo.estaInscritoEnClaseDeProfesor(id, actual.getId());
            if (inscrito) {
                log.info("👁️ Profesor accede al detalle de un alumno inscrito en su clase");
                return ResponseEntity.of(usuarioRepo.findById(id));
            } else {
                log.warn("⛔ Profesor intentó acceder a un alumno no inscrito en sus clases (ID {})", id);
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Elimina un usuario (ADMIN o el propio usuario).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (actual.getRol().name().equals("ADMIN") || actual.getId().equals(id)) {
            usuarioRepo.deleteById(id);
            log.info("🗑️ Usuario eliminado: ID {}", id);
            return ResponseEntity.ok().build();
        }

        log.warn("⛔ Usuario no autorizado para eliminar: ID {}", id);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Actualiza los datos de un usuario.
     * ADMIN puede editar todo. Un usuario puede editar sus propios datos.
     * PROFESOR solo puede modificar el estado ACTIVO de sus alumnos.
     *
     * @param id             ID del usuario a modificar
     * @param updatedUsuario Datos actualizados desde el frontend
     * @return El usuario actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario updatedUsuario) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean esAdmin = actual.getRol().name().equals("ADMIN");
        boolean esMismoUsuario = actual.getId().equals(id);

        // Recuperar el usuario de forma explícita
        var optionalUsuario = usuarioRepo.findById(id);

        if (optionalUsuario.isEmpty()) {
            log.warn("❌ Usuario ID={} no encontrado para actualización", id);
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = optionalUsuario.get();

        if (esAdmin || esMismoUsuario) {
            usuario.setNombre(updatedUsuario.getNombre());
            usuario.setApellido(updatedUsuario.getApellido());
            usuario.setTelefono(updatedUsuario.getTelefono());
            usuario.setDireccion(updatedUsuario.getDireccion());
            usuario.setFotoPerfil(updatedUsuario.getFotoPerfil());
            usuario.setFechaNacimiento(updatedUsuario.getFechaNacimiento());

            if (esAdmin) {
                usuario.setActivo(updatedUsuario.isActivo());
            }

            if (updatedUsuario.getContrasenna() != null && !updatedUsuario.getContrasenna().isBlank()) {
                usuario.setContrasenna(passwordEncoder.encode(updatedUsuario.getContrasenna()));
            }

            Usuario guardado = usuarioRepo.save(usuario);
            log.info("✅ Usuario actualizado correctamente por {} (ID={})", actual.getRol(), id);
            return ResponseEntity.ok(guardado);
        }

        if (actual.getRol().name().equals("PROFESOR")) {
            boolean inscrito = usuarioRepo.estaInscritoEnClaseDeProfesor(id, actual.getId());
            if (inscrito) {
                usuario.setActivo(updatedUsuario.isActivo());
                Usuario guardado = usuarioRepo.save(usuario);
                log.info("🔄 Profesor actualizó campo activo de usuario ID={}", id);
                return ResponseEntity.ok(guardado);
            }
        }

        log.warn("⛔ No autorizado para actualizar usuario ID={}", id);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Devuelve el usuario autenticado actual.
     */
    @GetMapping("/me")
    public ResponseEntity<Usuario> getMiPerfil() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        log.info("👤 Perfil solicitado para {}", usuario.getCorreo());
        return ResponseEntity.ok(usuario);
    }

    /**
     * Obtiene los alumnos inscritos en clases del profesor especificado.
     */
    @PreAuthorize("hasRole('PROFESOR')")
    @GetMapping("/profesor/{id}/alumnos")
    public ResponseEntity<?> getAlumnosPorProfesor(@PathVariable Long id) {
        try {
            List<UsuarioDTO> alumnos = usuarioRepo.findAlumnosPorProfesorId(id);
            log.info("📚 {} alumnos devueltos para profesor ID {}", alumnos.size(), id);
            return ResponseEntity.ok(alumnos);
        } catch (Exception e) {
            log.error("❌ Error al obtener alumnos del profesor ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener alumnos del profesor");
        }
    }

    /**
     * Método auxiliar para extraer el usuario autenticado del contexto de
     * seguridad.
     */
    private Usuario getUsuarioAutenticado() {
        try {
            UsuarioDetails details = (UsuarioDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            return details.getUsuario();
        } catch (Exception e) {
            log.error("❌ No se pudo obtener el usuario autenticado", e);
            return null;
        }
    }
}