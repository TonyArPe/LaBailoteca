package com.bailoteca.security;

import com.bailoteca.models.usuario.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Clase que implementa UserDetails para proporcionar detalles del usuario
 * autenticado en el contexto de seguridad de Spring.
 * Esta clase encapsula un objeto Usuario
 * y proporciona la información necesaria para la autenticación y autorización.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see UserDetails
 * @see Usuario
 * @see GrantedAuthority
 * @see SimpleGrantedAuthority
 * @see Collection
 * @see List
 */
@Getter
@AllArgsConstructor
public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    /**
     * Constructor que inicializa el UsuarioDetails con un objeto Usuario.
     *
     * @param usuario El objeto Usuario que contiene la información del usuario.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    /**
     * Obtiene la contraseña del usuario.
     * Si la contraseña es nula, devuelve una cadena vacía.
     *
     * @return La contraseña del usuario o una cadena vacía si es nula.
     */
    @Override
    public String getPassword() {
        return usuario.getContrasenna() != null ? usuario.getContrasenna() : "";
    }

/**
     * Obtiene el nombre de usuario del usuario.
     * En este caso, se utiliza el correo electrónico del usuario como nombre de usuario.
     *
     * @return El correo electrónico del usuario.
     */
    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    /**
     * Obtiene el ID del usuario.
     * Este método es específico de la implementación y no forma parte de UserDetails.
     *
     * @return El ID del usuario.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Verifica si la cuenta del usuario no está bloqueada.
     * En este caso, siempre devuelve true, ya que no se implementa lógica de bloqueo.
     *
     * @return true si la cuenta no está bloqueada, false en caso contrario.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Verifica si las credenciales del usuario no han expirado.
     * En este caso, siempre devuelve true, ya que no se implementa lógica de expiración de credenciales.
     *
     * @return true si las credenciales no han expirado, false en caso contrario.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Verifica si el usuario está habilitado.
     * En este caso, se basa en el estado activo del usuario.
     *
     * @return true si el usuario está activo, false en caso contrario.
     */
    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }
}