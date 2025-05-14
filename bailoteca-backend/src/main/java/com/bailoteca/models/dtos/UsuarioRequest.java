package com.bailoteca.models.dtos;

import com.bailoteca.models.enums.Rol;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO utilizado para registrar un nuevo usuario en el sistema.
 * Este DTO expone solo los campos necesarios desde el frontend
 * y permite mantener la entidad aislada.
 */

@Data
public class UsuarioRequest {
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasenna;
    private Rol rol = Rol.USUARIO;
    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
}