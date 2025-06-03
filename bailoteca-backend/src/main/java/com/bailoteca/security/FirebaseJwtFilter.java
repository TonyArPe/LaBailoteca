package com.bailoteca.security;

import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT de seguridad personalizado para validar tokens de Firebase.
 * Si el token es válido y el usuario está registrado, se autentica en el contexto de Spring.
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

        String path = request.getServletPath();
        log.debug("🛡️ Ruta interceptada por filtro JWT: {}", path);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("🚫 Cabecera Authorization ausente o malformada");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String email = decodedToken.getEmail();

            log.debug("✅ Token verificado. Email extraído: {}", email);

            Usuario usuario = usuarioRepo.findByCorreo(email).orElse(null);
            if (usuario == null) {
                log.warn("🚷 Usuario con correo '{}' no encontrado en BD", email);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Usuario no registrado");
                return;
            }

            UserDetails userDetails = new UsuarioDetails(usuario);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("🔐 Usuario autenticado: {} (ID: {})", usuario.getCorreo(), usuario.getId());

        } catch (FirebaseAuthException e) {
            log.error("❌ Error al verificar token Firebase: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        } catch (Exception e) {
            log.error("🔥 Error inesperado en filtro FirebaseJwt: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error en autenticación");
            return;
        }

        filterChain.doFilter(request, response);
    }
}