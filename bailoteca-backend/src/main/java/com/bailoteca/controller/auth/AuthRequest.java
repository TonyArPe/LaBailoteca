package com.bailoteca.controller.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Clase que representa la solicitud de autenticación.
 * Contiene los campos necesarios para iniciar sesión: correo y contraseña.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
public class AuthRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenna;
}
