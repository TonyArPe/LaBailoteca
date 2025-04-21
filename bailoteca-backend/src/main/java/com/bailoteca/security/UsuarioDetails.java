package com.bailoteca.security;

import com.bailoteca.models.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación de UserDetails para adaptar la entidad Usuario
 * al sistema de autenticación de Spring Security.
 */
@RequiredArgsConstructor
public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    /**
     * Devuelve el rol del usuario como autoridad de Spring Security.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    /**
     * Devuelve la contraseña del usuario.
     */
    @Override
    public String getPassword() {
        return usuario.getContrasenna();
    }

    /**
     * Devuelve el identificador del usuario (correo).
     */
    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }

    /**
     * Devuelve el usuario original encapsulado.
     */
    public Usuario getUsuario() {
        return usuario;
    }
}
