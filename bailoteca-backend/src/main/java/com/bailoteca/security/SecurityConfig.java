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
 * Configuración de seguridad para la aplicación.
 * Define las reglas de acceso a los endpoints, maneja la autenticación
 * y autorización de usuarios, y configura el filtro JWT de Firebase.
 * Esta clase utiliza Spring Security para proteger los recursos
 * y gestionar el acceso basado en roles.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see FirebaseJwtFilter
 * @see SecurityFilterChain
 * @see AuthenticationManager
 * @see PasswordEncoder
 * @see AccessDeniedHandler
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final FirebaseJwtFilter firebaseJwtFilter;

    /**
     * Configura la cadena de filtros de seguridad para la aplicación.
     * Define las reglas de autorización, manejo de sesiones y excepciones.
     * Permite el acceso a ciertos endpoints sin autenticación,
     * mientras que otros requieren roles específicos o autenticación general.
     *
     * @param http la configuración de seguridad HTTP
     * @return la cadena de filtros de seguridad configurada
     * @throws Exception si ocurre un error al configurar la seguridad
     */
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
                .requestMatchers(
                "/api/clases/publicas",
                 "/api/eventos/publicos",
                 "/api/uploads/files/**",
                 "/media/**").permitAll()

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

    /**
     * Maneja los accesos denegados, enviando una respuesta JSON con un mensaje de error.
     * Este método se invoca cuando un usuario intenta acceder a un recurso sin los permisos adecuados.
     *
     * @return el manejador de acceso denegado
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
     * Proporciona el AuthenticationManager para la autenticación de usuarios.
     * Este bean es necesario para que Spring Security pueda gestionar la autenticación
     * de los usuarios utilizando el filtro JWT y otros mecanismos de autenticación.
     *
     * @param config la configuración de autenticación
     * @return el AuthenticationManager configurado
     * @throws Exception si ocurre un error al obtener el AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Proporciona un PasswordEncoder para codificar contraseñas.
     * Utiliza BCrypt como algoritmo de codificación, que es seguro y ampliamente utilizado.
     * Este bean es necesario para que Spring Security pueda gestionar las contraseñas de los usuarios.
     *
     * @return el PasswordEncoder configurado
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}