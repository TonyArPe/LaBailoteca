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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de seguridad que intercepta las solicitudes HTTP para verificar el token JWT de Firebase.
 * Autentica al usuario y lo agrega al contexto de seguridad si el token es válido.
 * Excluye ciertas rutas del filtrado para permitir acceso anónimo, como registro, login o acceso público.
 * 
 * @author Tony Aragón
 * @version 1.1
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseJwtFilter extends OncePerRequestFilter {

    private final UsuarioRepo usuarioRepo;

    // Rutas excluidas del filtrado JWT para acceso anónimo o público.
    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/",
            "/api/usuarios", // Se añade explícitamente para permitir POST de registro
            "/api/auth/**",
            "/api/eventos/publicos",
            "/api/clases/publicas",
            "/media/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Procesa cada solicitud HTTP entrante. Si la ruta es pública, omite la validación JWT.
     * Si la ruta requiere autenticación, valida el token Firebase y autentica al usuario.
     *
     * @param request     La solicitud HTTP entrante.
     * @param response    La respuesta HTTP correspondiente.
     * @param filterChain La cadena de filtros a ejecutar.
     * @throws ServletException en caso de error interno.
     * @throws IOException      en caso de error de E/S.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        log.debug("🛡️ Ruta interceptada por filtro JWT: {} [{}]", path, method);

        // Si la ruta está excluida, no validamos el token
        if (isExcluded(path, method)) {
            log.debug("🟢 Ruta pública detectada → omitiendo validación JWT");
            filterChain.doFilter(request, response);
            return;
        }

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

            // Si el usuario está desactivado, retornamos un error 401
            if (!usuario.isActivo()) {
                log.warn("⛔ Usuario {} está desactivado", usuario.getCorreo());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario desactivado");
                return;
            }

            // Si el usuario es válido, lo autenticamos en el contexto de seguridad
            UserDetails userDetails = new UsuarioDetails(usuario);
            var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Establecemos la autenticación en el contexto de seguridad
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

    /**
     * Determina si una ruta está excluida de la autenticación JWT, evaluando tanto el path como el método.
     *
     * @param path   Ruta de la solicitud (ej. "/api/usuarios")
     * @param method Método HTTP de la solicitud (ej. POST, GET)
     * @return true si debe excluirse del filtro, false si debe validarse
     */
    private boolean isExcluded(String path, String method) {
        return EXCLUDE_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path))
                || (path.equals("/api/usuarios") && method.equals("POST")); // Registro permitido explícitamente
    }
}