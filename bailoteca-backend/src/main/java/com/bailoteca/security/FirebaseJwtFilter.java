package com.bailoteca.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * Filtro personalizado que intercepta cada petición entrante y valida el token
 * JWT emitido por Firebase.
 * Si el token es válido, se establece la autenticación en el contexto de
 * seguridad de Spring.
 */
@Slf4j
@Component
public class FirebaseJwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.info("🚨 Entrando al filtro FirebaseJwtFilter...");

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("⚠️  No hay token en el header Authorization o no comienza con 'Bearer '");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            log.info("🟡 Token recibido: {}...", token.substring(0, Math.min(token.length(), 30)));

            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String email = firebaseToken.getEmail();
            String customRole = (String) firebaseToken.getClaims().get("role");

            if (email == null || email.isBlank()) {
                log.error("❌ El token no contiene un email válido");
                filterChain.doFilter(request, response);
                return;
            }

            log.info("📧 Email del token: {}", email);
            log.info("🛡️ Rol recibido (si existe): {}", customRole);

            // Se construyen las autoridades desde el rol o se asigna ROLE_USER por defecto
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(customRole != null ? "ROLE_" + customRole : "ROLE_USER")
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    email, null, authorities
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            log.info("Antes de setAuthentication: contexto = {}", SecurityContextHolder.getContext().getAuthentication());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Después de setAuthentication: autenticado = {}", SecurityContextHolder.getContext().getAuthentication().getName());

        } catch (Exception ex) {
            log.error("❌ Error verificando el token Firebase: {}", ex.getMessage());
        }

        log.info("✅ Saliendo del filtro FirebaseJwtFilter...");
        filterChain.doFilter(request, response);
    }
}