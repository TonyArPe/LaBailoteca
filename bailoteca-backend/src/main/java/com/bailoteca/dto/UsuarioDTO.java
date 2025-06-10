package com.bailoteca.dto;

import lombok.Getter;
import lombok.Setter;


/**
 * DTO de salida para exponer datos públicos del usuario.
 * Este objeto se utiliza para enviar información del usuario
 * desde el servidor al cliente,
 * incluyendo su ID, nombre, apellido y correo electrónico.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see UsuarioDTO
 * @see Usuario
 */
@Getter
@Setter
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellido = "";
    private String correo;

    public UsuarioDTO(Long id, String nombre, String apellido, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
    }

    public UsuarioDTO() {
    }
}
