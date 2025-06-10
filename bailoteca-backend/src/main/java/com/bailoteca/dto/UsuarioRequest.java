
package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO de entrada para crear o actualizar un usuario desde cliente.
 * Este objeto se utiliza para enviar datos desde el cliente al servidor
 * para crear o actualizar la información de un usuario,
 * incluyendo su nombre, apellido, correo electrónico,
 * contraseña, foto de perfil, teléfono, dirección, fecha de nacimiento,
 * género y DNI.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see UsuarioRequest
 * @see Usuario
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