package com.bailoteca.controller.auth;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;

import java.util.Map;

/**
 * Controlador que gestiona la autenticación de usuarios.
 * Permite iniciar sesión y obtener un JWT.
 * Este controlador se encarga de validar las credenciales del usuario
 * y devolver un token JWT si son correctas.
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Usuario
 * @see UsuarioRepo
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepo usuarioRepo;

    /**
     * Endpoint para iniciar sesión.
     * 
     * @param request contiene el correo y la contraseña del usuario
     * @return JWT si la autenticación es válida
     */
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Map<String, String> payload) {
        String correo = payload.get("correo");

        return usuarioRepo.findByCorreo(correo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

}
