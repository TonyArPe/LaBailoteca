
package com.bailoteca.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Clase que representa una solicitud de actualización de un usuario administrador.
 * Contiene información sobre el nombre, apellido, teléfono, dirección, foto de perfil,
 * fecha de nacimiento, estado activo y si ha pagado.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
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