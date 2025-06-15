package com.bailoteca.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Clase que representa un usuario en el sistema.
 * Contiene información básica del usuario como su ID, nombre, apellido y correo electrónico.
 * 
 * Esta clase es utilizada como DTO para transportar datos entre capas,
 * y también como proyección JPQL en consultas personalizadas.
 * 
 * ⚠️ IMPORTANTE: Si usas `SELECT new ...` en JPQL, necesitas constructores explícitos.
 * 
 * @author Tony Aragón
 * @version 1.1
 * @since 1.0
 */
@Getter
@Setter
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellido = "";
    private String correo;
    private String rol;

    /**
     * Constructor completo para DTOs que requieren el campo rol.
     */
    public UsuarioDTO(Long id, String nombre, String apellido, String correo, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
    }

    /**
     * Constructor utilizado por JPQL en consultas con proyección parcial
     * como: SELECT new UsuarioDTO(u.id, u.nombre, u.apellido, u.correo)
     */
    public UsuarioDTO(Long id, String nombre, String apellido, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
    }

    /**
     * Constructor vacío necesario para serialización/deserialización.
     */
    public UsuarioDTO() {
    }
}