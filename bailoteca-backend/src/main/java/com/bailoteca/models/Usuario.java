package com.bailoteca.models;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa a un usuario del sistema.
 * Tiene distintos Roles: ADMIN, PROFESOR, USUARIO, INVITADO.
 * Contiene informacion del perfil y estado del Usuario.
 */

 @Entity
 @Table(name = "usuarios")
@Data
 @NoArgsConstructor
 @AllArgsConstructor
 @Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String correo;
    private String contrasenna;
    private Rol rol;
    private String fotoPerfil;
    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String genero;
    private String dni;
    private LocalDate fechaRegistro;
    private boolean activo;
    private boolean pagado;
}
