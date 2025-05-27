package com.bailoteca.controller.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.bailoteca.dto.UsuarioDTO;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.security.UsuarioDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Controlador REST para gestionar Usuarios en el sistema.
 */

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepo usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios del sistema (solo ADMIN).
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping
    public ResponseEntity<List<Usuario>> getUsuarios() {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (actual.getRol().name().equals("ADMIN")) {
            return ResponseEntity.ok(usuarioRepo.findAll());
        }

        if (actual.getRol().name().equals("PROFESOR")) {
            return ResponseEntity.ok(usuarioRepo.findAlumnosPorProfesor(actual.getId()));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Crea un nuevo usuario en el sistema. La contraseña se codifica automáticamente.
     */
    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario) {
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setActivo(true);
        usuario.setPagado(false);
        usuario.setContrasenna(passwordEncoder.encode(usuario.getContrasenna()));
        return usuarioRepo.save(usuario);
    }

    /**
     * Obtiene un usuario por su ID (ADMIN o el propio usuario).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (actual.getRol().name().equals("ADMIN") || actual.getId().equals(id)) {
            return ResponseEntity.of(usuarioRepo.findById(id));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Elimina un usuario por su ID (ADMIN o el propio usuario).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (actual.getRol().name().equals("ADMIN") || actual.getId().equals(id)) {
            usuarioRepo.deleteById(id);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Actualiza un usuario por su ID (ADMIN o el propio usuario).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario updatedUsuario) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (actual.getRol().name().equals("ADMIN") || actual.getId().equals(id)) {
            return usuarioRepo.findById(id)
                    .map(usuario -> {
                        usuario.setNombre(updatedUsuario.getNombre());
                        usuario.setApellido(updatedUsuario.getApellido());
                        usuario.setTelefono(updatedUsuario.getTelefono());
                        usuario.setDireccion(updatedUsuario.getDireccion());
                        usuario.setFotoPerfil(updatedUsuario.getFotoPerfil());
                        usuario.setFechaNacimiento(updatedUsuario.getFechaNacimiento());

                        if (actual.getRol().name().equals("ADMIN")) {
                            usuario.setActivo(updatedUsuario.isActivo());
                        }

                        if (updatedUsuario.getContrasenna() != null && !updatedUsuario.getContrasenna().isBlank()) {
                            usuario.setContrasenna(passwordEncoder.encode(updatedUsuario.getContrasenna()));
                        }

                        return ResponseEntity.ok(usuarioRepo.save(usuario));
                    })
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Devuelve el perfil del usuario autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<Usuario> getMiPerfil() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(usuario);
    }

    /**
     * Devuelve los alumnos inscritos en clases del profesor dado.
     * Si no hay alumnos, se devuelve una lista vacía (HTTP 200).
     */
    @PreAuthorize("hasRole('PROFESOR')")
    @GetMapping("/profesor/{id}/alumnos")
    public ResponseEntity<?> getAlumnosPorProfesor(@PathVariable Long id) {
        try {
            List<UsuarioDTO> alumnos = usuarioRepo.findAlumnosPorProfesorId(id);
            return ResponseEntity.ok(alumnos); // lista vacía si no hay inscripciones
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener alumnos del profesor");
        }
    }

    /**
     * Método auxiliar para obtener el usuario autenticado actual.
     */
    private Usuario getUsuarioAutenticado() {
        try {
            UsuarioDetails details = (UsuarioDetails) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            return details.getUsuario();
        } catch (Exception e) {
            return null;
        }
    }
}