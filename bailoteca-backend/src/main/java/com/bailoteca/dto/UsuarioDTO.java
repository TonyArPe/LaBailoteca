
package com.bailoteca.dto;

import lombok.Getter;
import lombok.Setter;


/**
 * DTO de salida para exponer datos públicos del usuario.
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
