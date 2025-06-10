package com.bailoteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Clase que representa una solicitud de registro de usuario.
 * Contiene los campos necesarios para crear un nuevo usuario: nombre, apellido, correo y contraseña.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@Data
public class RegistroRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @Email
    @NotBlank
    private String correo;

    @NotBlank
    private String contrasenna;
}