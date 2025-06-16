package com.bailoteca.controller.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.bailoteca.dto.UsuarioDTO;
import com.bailoteca.dto.UsuarioUpdateRequest;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.security.UsuarioDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador que maneja las operaciones relacionadas con los usuarios.
 * Permite registrar, consultar, actualizar y eliminar usuarios del sistema.
 * 
 * Se adapta la salida de usuarios a UsuarioDTO para evitar errores de permisos y 
 * exposición innecesaria de datos sensibles como contraseñas.
 * 
 * @author Tony Aragón
 * @version 1.1
 */
@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepo usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Devuelve el usuario autenticado (/me).
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Usuario> getUsuarioActual() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("👤 [GET /me] Usuario autenticado: {} (ID: {})", usuario.getCorreo(), usuario.getId());
        return ResponseEntity.ok(usuario);
    }

    /**
     * Devuelve la lista de usuarios visibles según el rol del usuario actual.
     * ADMIN: todos como DTO. PROFESOR: solo sus alumnos como entidades.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<?> getUsuarios() {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (actual.getRol().name().equals("ADMIN")) {
            log.info("👤 ADMIN solicitó la lista de todos los usuarios");
            List<UsuarioDTO> usuarios = usuarioRepo.findAll()
                    .stream()
                    .map(u -> new UsuarioDTO(u.getId(), u.getNombre(), u.getApellido(), u.getCorreo(), u.getRol().name()))
                    .toList();
            return ResponseEntity.ok(usuarios);
        }

        if (actual.getRol().name().equals("PROFESOR")) {
            log.info("👨‍🏫 PROFESOR solicitó la lista de sus alumnos con datos completos");

            List<Long> ids = usuarioRepo.findAlumnosPorProfesorId(actual.getId())
                    .stream()
                    .map(UsuarioDTO::getId)
                    .toList();

            List<Usuario> alumnos = usuarioRepo.findAllById(ids);
            log.info("📚 Profesor recibirá {} alumnos con información completa", alumnos.size());
            return ResponseEntity.ok(alumnos);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDTO createUsuario(@RequestBody Usuario usuario) {
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

        return new UsuarioDTO(
                registrado.getId(),
                registrado.getNombre(),
                registrado.getApellido(),
                registrado.getCorreo(),
                registrado.getRol().name());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null) {
            log.warn("⛔ Usuario no autenticado intentó acceder al detalle de ID {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean esAdmin = actual.getRol().name().equals("ADMIN");
        boolean esMismoUsuario = actual.getId().equals(id);

        if (esAdmin || esMismoUsuario) {
            log.info("👁️ Acceso permitido a usuario con ID {} por {}", id, actual.getCorreo());
            return ResponseEntity.of(usuarioRepo.findById(id));
        }

        if (actual.getRol().name().equals("PROFESOR")) {
            boolean inscrito = usuarioRepo.estaInscritoEnClaseDeProfesor(id, actual.getId());
            if (inscrito) {
                log.info("👨‍🏫 Profesor {} accede al detalle de alumno inscrito (ID={})", actual.getCorreo(), id);
                return ResponseEntity.of(usuarioRepo.findById(id));
            } else {
                log.warn("⛔ Profesor {} intentó acceder a un alumno no inscrito (ID={})",
                        actual.getCorreo(), id);
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario updatedUsuario) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean esAdmin = actual.getRol().name().equals("ADMIN");
        boolean esMismoUsuario = actual.getId().equals(id);

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

    @GetMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Usuario> getPerfilUsuario() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        log.info("👤 Perfil solicitado para {}", usuario.getCorreo());
        return ResponseEntity.ok(usuario);
    }

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

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<Usuario> actualizarEstadoUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioUpdateRequest request) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var optionalUsuario = usuarioRepo.findById(id);
        if (optionalUsuario.isEmpty()) {
            log.warn("❌ Usuario ID={} no encontrado", id);
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = optionalUsuario.get();
        boolean esAdmin = actual.getRol().name().equals("ADMIN");
        boolean esProfesor = actual.getRol().name().equals("PROFESOR");

        if (esAdmin) {
            if (request.getActivo() != null)
                usuario.setActivo(request.getActivo());
            if (request.getPagado() != null)
                usuario.setPagado(request.getPagado());
            log.info("✅ ADMIN actualizó usuario ID={} [activo={}, pagado={}]", id, request.getActivo(), request.getPagado());
        } else if (esProfesor) {
            boolean inscrito = usuarioRepo.estaInscritoEnClaseDeProfesor(id, actual.getId());
            if (!inscrito) {
                log.warn("⛔ Profesor no autorizado para usuario ID={}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (request.getPagado() != null) {
                usuario.setPagado(request.getPagado());
                log.info("🔄 PROFESOR cambió pagado ID={} a {}", id, request.getPagado());
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Usuario actualizado = usuarioRepo.save(usuario);
        return ResponseEntity.ok(actualizado);
    }
}