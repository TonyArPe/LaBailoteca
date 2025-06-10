package com.bailoteca.controller.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para representar la solicitud de autenticación.
 * Contiene el correo y la contraseña del usuario.
 * Se utiliza para validar las credenciales al iniciar sesión.
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
