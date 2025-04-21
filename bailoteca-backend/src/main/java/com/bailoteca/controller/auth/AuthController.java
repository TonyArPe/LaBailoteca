package com.bailoteca.controller.auth;

import com.bailoteca.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador que gestiona la autenticación de usuarios.
 * Permite iniciar sesión y obtener un JWT.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Endpoint para iniciar sesión.
     * 
     * @param request contiene el correo y la contraseña del usuario
     * @return JWT si la autenticación es válida
     */
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody AuthRequest request) {
        // Autentica al usuario usando AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasenna())
        );

        // Genera un token JWT para el usuario autenticado
        String jwt = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());

        return Map.of("token", jwt);
    }
}
