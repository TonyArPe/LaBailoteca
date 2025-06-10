package com.bailoteca.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuración principal de seguridad para la aplicación Bailoteca.
 * Define accesos permitidos por ruta y protege los endpoints mediante JWT de Firebase.
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final FirebaseJwtFilter firebaseJwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // Rutas públicas (sin token)
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/clases/publicas", "/api/eventos/publicos").permitAll()

                // Usuarios
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers("/api/usuarios/**").authenticated()

                // Eventos
                .requestMatchers(HttpMethod.GET, "/api/eventos/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/eventos").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.PUT, "/api/eventos/**").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos/**").hasAnyRole("ADMIN", "PROFESOR")

                // Asistencia a eventos (solo usuarios)
                .requestMatchers(HttpMethod.POST, "/api/eventos/*/asistir").hasRole("USUARIO")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos/*/asistir").hasRole("USUARIO")

                // Ver asistentes (solo admin o profesor creador)
                .requestMatchers(HttpMethod.GET, "/api/eventos/*/asistentes").hasAnyRole("ADMIN", "PROFESOR")

                // Recursos públicos
                .requestMatchers("/", "/index.html", "/chat.html", "/ws/**").permitAll()

                // Todo lo demás requiere autentificación
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler()))
            .addFilterBefore(firebaseJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Manejador de errores personalizados para accesos denegados.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Access Denied: No tienes permisos suficientes\"}");
        };
    }

    /**
     * Bean para autenticación basada en configuración de seguridad.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean para codificar contraseñas (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}