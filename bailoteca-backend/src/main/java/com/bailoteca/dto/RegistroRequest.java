package com.bailoteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la solicitud de registro de un nuevo usuario.
 * Contiene los campos necesarios para crear una cuenta de usuario,
 * incluyendo nombre, apellido, correo electrónico y contraseña.
 * Este objeto se utiliza para enviar datos desde el cliente al servidor
 * para registrar un nuevo usuario en la aplicación.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Usuario
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