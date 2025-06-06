package com.bailoteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la solicitud de registro de un nuevo usuario.
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