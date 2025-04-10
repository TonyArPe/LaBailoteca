package com.bailoteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad temporal para desarrollo.
 * Permite el acceso a todos los endpoints sin autenticación.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configura la seguridad para permitir acceso sin restricciones.
     * 
     * @param http configuración de seguridad HTTP
     * @return filtro de seguridad configurado
     * @throws Exception si ocurre un error durante la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactiva protección CSRF
                .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll() // Permite TODO
                );
        return http.build();
    }
}
