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
 * Filtro de seguridad que intercepta las solicitudes HTTP para verificar el
 * token JWT de Firebase.
 * Autentica al usuario y lo agrega al contexto de seguridad si el token es
 * válido.
 * Excluye ciertas rutas del filtrado para permitir acceso anónimo.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Usuario
 * @see UsuarioRepo
 * @see FirebaseAuth
 * @see FirebaseToken
 * @see UserDetails
 * @see UsernamePasswordAuthenticationToken
 * @see OncePerRequestFilter
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseJwtFilter extends OncePerRequestFilter {

    private final UsuarioRepo usuarioRepo;

    /**
     * Rutas que se excluyen del filtrado JWT.
     * Estas rutas permiten acceso anónimo y no requieren autenticación.
     */
    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/",
            "/api/auth/**",
            "/api/eventos/publicos",
            "/api/clases/publicas",
            "/media/**");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Método que se ejecuta para filtrar las solicitudes HTTP.
     * Verifica el token JWT y autentica al usuario si es válido.
     * Si la ruta está excluida, omite el filtrado.
     *
     * @param request     La solicitud HTTP entrante.
     * @param response    La respuesta HTTP a enviar.
     * @param filterChain La cadena de filtros a seguir.
     * @throws ServletException Si ocurre un error en el procesamiento del filtro.
     * @throws IOException      Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        log.debug("🛡️ Ruta interceptada por filtro JWT: {}", path);

        if (isExcluded(path, request.getMethod())) {
            log.debug("🟢 Ruta pública, omitiendo filtro JWT");
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

            if (!usuario.isActivo()) {
                log.warn("⛔ Usuario {} está desactivado y no puede acceder", usuario.getCorreo());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario desactivado");
                return;
            }

            UserDetails userDetails = new UsuarioDetails(usuario);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());

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
     * Verifica si la ruta y el método HTTP están excluidos del filtrado JWT.
     * Permite acceso anónimo a ciertas rutas específicas.
     *
     * @param path   La ruta de la solicitud.
     * @param method El método HTTP de la solicitud.
     * @return true si la ruta está excluida, false en caso contrario.
     */
    private boolean isExcluded(String path, String method) {
        return EXCLUDE_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}