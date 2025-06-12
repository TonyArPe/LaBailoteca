
package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Clase que representa una solicitud de creación o actualización de un usuario.
 * Contiene información personal del usuario como nombre, apellido, correo electrónico,
 * contraseña, foto de perfil, teléfono, dirección, fecha de nacimiento, género y DNI.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@Data
public class UsuarioRequest {
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasenna;
    private String fotoPerfil;
    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String genero;
    private String dni;
}