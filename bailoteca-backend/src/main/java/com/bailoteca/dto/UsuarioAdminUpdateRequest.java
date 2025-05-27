
package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO de actualización para administradores (pueden cambiar rol y estado).
 */
@Data
public class UsuarioAdminUpdateRequest {
    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
    private String fotoPerfil;
    private LocalDate fechaNacimiento;
    private boolean activo;
    private boolean pagado;
}