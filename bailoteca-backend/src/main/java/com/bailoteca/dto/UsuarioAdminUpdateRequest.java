
package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO de actualización para administradores (pueden cambiar rol y estado).
 * Este objeto se utiliza para enviar datos desde el cliente al servidor
 * para actualizar la información de un usuario administrador,
 * incluyendo su nombre, apellido, teléfono, dirección, foto de perfil,
 * fecha de nacimiento, estado activo y si ha pagado.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see UsuarioAdminUpdateRequest
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