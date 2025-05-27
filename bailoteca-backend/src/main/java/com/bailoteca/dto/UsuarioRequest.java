
package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO de entrada para crear o actualizar un usuario desde cliente.
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