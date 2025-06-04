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
 * Configuración principal de seguridad para Bailoteca.
 * Se utiliza autenticación JWT (Firebase) y control de roles.
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

                // REGISTRO PERMITIDO SIN TOKEN
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()

                // LOGIN PERMITIDO SIN TOKEN
                .requestMatchers("/api/auth/**").permitAll()

                // EVENTOS Y CLASES VISIBLES PARA INVITADOS
                .requestMatchers("/api/clases/publicas", "/api/eventos/publicos").permitAll()

                // GET de usuarios requiere rol
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAnyRole("ADMIN", "PROFESOR")

                // Cualquier acción específica sobre usuarios requiere autenticación
                .requestMatchers("/api/usuarios/**").authenticated()

                // Recursos frontend o públicos
                .requestMatchers("/", "/index.html", "/chat.html", "/ws/**").permitAll()

                // Todo lo demás requiere estar autenticado
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler()))
            .addFilterBefore(firebaseJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Access Denied: No tienes permisos suficientes\"}");
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}