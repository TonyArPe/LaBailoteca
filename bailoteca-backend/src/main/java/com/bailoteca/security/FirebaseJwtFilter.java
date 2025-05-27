package com.bailoteca.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import lombok.RequiredArgsConstructor;

/**
 * Filtro de seguridad que intercepta todas las peticiones HTTP para validar tokens JWT emitidos por Firebase.
 * Si el token es válido y el usuario está registrado en la base de datos, se autentica en Spring Security.
 * En caso contrario, se bloquea la petición con un código 403 (prohibido).
 */
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
        System.out.println("🛡️ FILTRO FIREBASE - Ruta accedida: " + path);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ FILTRO FIREBASE - Cabecera Authorization ausente o malformada");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("🔐 FILTRO FIREBASE - Token extraído: " + token);

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String email = decodedToken.getEmail();
            System.out.println("✅ FILTRO FIREBASE - Email verificado en token: " + email);

            Usuario usuario = usuarioRepo.findByCorreo(email).orElse(null);

            if (usuario == null) {
                System.out.println("⛔ FILTRO FIREBASE - Usuario no registrado en la base de datos");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Usuario no registrado en backend");
                return; // ← CORTAMOS LA CADENA
            }

            // Autenticación manual
            UserDetails userDetails = new User(
                    usuario.getCorreo(),
                    usuario.getContrasenna() != null ? usuario.getContrasenna() : "",
                    Collections.emptyList()
            );

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("✅ FILTRO FIREBASE - Usuario autenticado correctamente: " + usuario.getCorreo());

        } catch (FirebaseAuthException e) {
            System.out.println("❌ FILTRO FIREBASE - Token inválido: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        } catch (Exception e) {
            System.out.println("❌ FILTRO FIREBASE - Error inesperado: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno");
            return;
        }

        filterChain.doFilter(request, response);
    }
}