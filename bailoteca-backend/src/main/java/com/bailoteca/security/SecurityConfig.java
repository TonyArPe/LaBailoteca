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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuración principal de seguridad Spring.
 * Usa JWT de Firebase y define reglas para el acceso a endpoints según roles.
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

                // SOLO PERMITIR POST para crear usuarios (registro abierto)
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()

                // GET requiere autenticación y roles
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAnyRole("ADMIN", "PROFESOR")

                // Otros métodos sobre /api/usuarios deben ser autenticados
                .requestMatchers("/api/usuarios/**").authenticated()

                // Rutas públicas para invitados
                .requestMatchers(
                    "/", "/index.html", "/chat.html", "/chat-privado.html", "/notificaciones.html",
                    "/ws/**", "/api/auth/**", "/api/clases/publicas", "/api/eventos/publicos"
                ).permitAll()

                // Todo lo demás también requiere autenticación
                .anyRequest().authenticated()
            )
            // Manejo de errores de acceso denegado
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler())
            )
            // Añadimos el filtro JWT antes del de username/contraseña
            .addFilterBefore(firebaseJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Manejador global para evitar errores 500 por errores de acceso denegado.
     * Devuelve 403 con mensaje personalizado en JSON.
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
     * Permite usar AuthenticationManager si en el futuro se quiere hacer login manual.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Codificador de contraseñas para usuarios.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}