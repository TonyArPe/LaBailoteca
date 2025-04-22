package com.bailoteca.controller.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar Usuarios.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    /**
     * Repositorio de usuarios para operaciones CRUD.
     */
    private final UsuarioRepo usuarioRepo;

    /**
     * Codificador de contraseñas (BCrypt).
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios del sistema (solo ADMIN).
     * llega desde el token JWT
     * 
     * @return Lista de usuarios
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Usuario> getUsuarios() {
        return usuarioRepo.findAll();
    }

    /**
     * Crea un nuevo usuario en el sistema. La contraseña se codifica
     * automáticamente.
     * 
     * @param usuario Usuario a crear
     * @return Usuario creado
     */
    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario) {
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setContrasenna(passwordEncoder.encode(usuario.getContrasenna())); // 🔐 Encriptar antes de guardar
        return usuarioRepo.save(usuario);
    }

    /**
     * Obtiene un usuario por su ID.
     * 
     * @param id ID del usuario a obtener
     * @return Usuario encontrado o null si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        // Obtener el usuario autenticado (correo)
        String correoAuth = ((UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()).getUsername();

        // Buscar quién está autenticado
        Usuario actual = usuarioRepo.findByCorreo(correoAuth).orElse(null);
        if (actual == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Si es ADMIN, permitir
        if (actual.getRol().name().equals("ADMIN")) {
            return ResponseEntity.of(usuarioRepo.findById(id));
        }

        // Si el ID solicitado es el suyo propio, permitir
        if (actual.getId().equals(id)) {
            return ResponseEntity.of(usuarioRepo.findById(id));
        }

        // Si no es ni ADMIN ni el mismo usuario, denegar
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Elimina un usuario por su ID.
     * 
     * @param id del Usuario a borrar
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        // Obtener el usuario autenticado (correo)
        String correoAuth = ((UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()).getUsername();

        Usuario actual = usuarioRepo.findByCorreo(correoAuth).orElse(null);
        if (actual == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Permitir si es ADMIN o si el usuario borra su propia cuenta
        if (actual.getRol().name().equals("ADMIN") || actual.getId().equals(id)) {
            usuarioRepo.deleteById(id);
            return ResponseEntity.ok().build();
        }

        // Denegar en cualquier otro caso
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Devuelve el perfil del usuario autenticado.
     * extrae el correo desde el JWT, busca el usuario en base de datos y lo
     * devuelve.
     * 
     * @return Datos del usuario actual
     */
    @GetMapping("/me")
    public Usuario getMiPerfil() {
        String correo = ((UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()).getUsername();

        return usuarioRepo.findByCorreo(correo).orElse(null);
    }
}