package com.bailoteca.security;

import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que valida tokens Firebase y configura el contexto de seguridad.
 * Asigna por defecto ROLE_USER si no hay rol explícito en el token.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseJwtFilter extends OncePerRequestFilter {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        log.debug("Verificando si se debe aplicar filtro para la ruta: {}", path);
        return path.equals("/api/usuarios/firebase");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No se encontró header Authorization válido.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String email = firebaseToken.getEmail();

            log.debug("Token verificado. Email extraído: {}", email);

            if (email == null || email.isBlank()) {
                log.warn("El email del token es nulo o vacío.");
                filterChain.doFilter(request, response);
                return;
            }

            Usuario usuario = usuarioRepo.findByCorreo(email).orElse(null);
            if (usuario == null) {
                log.warn("El usuario con correo {} no existe en la base de datos.", email);
                filterChain.doFilter(request, response);
                return;
            }

            UsuarioDetails usuarioDetails = new UsuarioDetails(usuario);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(usuarioDetails, null, usuarioDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Usuario autenticado correctamente: {}", email);

        } catch (Exception e) {
            log.error("Error al verificar token Firebase: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}