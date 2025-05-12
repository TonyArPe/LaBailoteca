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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que valida tokens Firebase y configura el contexto de seguridad.
 * Asigna por defecto ROLE_USER si no hay rol explícito en el token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseJwtFilter extends OncePerRequestFilter {

    private final UsuarioRepo usuarioRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String email = firebaseToken.getEmail();

            if (email == null || email.isBlank()) {
                log.error("❌ El token no contiene un email válido");
                filterChain.doFilter(request, response);
                return;
            }

            Usuario usuario = usuarioRepo.findByCorreo(email).orElse(null);
            if (usuario == null) {
                log.error("❌ Usuario no encontrado en la base de datos");
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("✅ Usuario autenticado: {} con rol {}", email, usuario.getRol());

        } catch (Exception ex) {
            log.error("❌ Error verificando el token Firebase: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}