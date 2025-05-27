package com.bailoteca.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FirebaseJwtFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        System.out.println("FILTRO FIREBASE Requiere autenticación: " + path);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("FILTRO FIREBASE No se encontró cabecera Authorization válida");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("FILTRO FIREBASE Token recibido: " + token);

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String email = decodedToken.getEmail();
            System.out.println("FILTRO FIREBASE Email extraído del token: " + email);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            System.out.println("FILTRO FIREBASE Usuario encontrado en la base de datos: " + userDetails.getUsername());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("FILTRO FIREBASE Usuario autenticado correctamente");

        } catch (FirebaseAuthException e) {
            System.out.println("FILTRO FIREBASE Token inválido: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("FILTRO FIREBASE Error al autenticar usuario: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}