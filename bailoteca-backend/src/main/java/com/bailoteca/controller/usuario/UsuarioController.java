package com.bailoteca.controller.usuario;

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
     * Obtiene todos los usuarios del sistema.
     * 
     * @return Lista de usuarios
     */
    @GetMapping
    public List<Usuario> getUsuarios() {
        return usuarioRepo.findAll();
    }

    /**
     * Crea un nuevo usuario en el sistema. La contraseña se codifica automáticamente.
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
    public Usuario getUsuario(@PathVariable Long id) {
        return usuarioRepo.findById(id).orElse(null);
    }

    /**
     * Elimina un usuario por su ID.
     * 
     * @param id del Usuario a borrar
     */
    @DeleteMapping("/{id}")
    public void deleteUsuario(@PathVariable Long id) {
        usuarioRepo.deleteById(id);
    }
}