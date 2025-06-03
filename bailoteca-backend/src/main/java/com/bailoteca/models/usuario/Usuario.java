package com.bailoteca.models.usuario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.bailoteca.models.enums.Rol;
import com.bailoteca.models.inscripcion.Inscripcion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
@JsonInclude(Include.NON_NULL)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;

    @Column(unique = true)
    private String correo;
    private String contrasenna;

    private Rol rol;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Evita recursion infinita en API REST
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Inscripcion> inscripciones = new ArrayList<>();

    private String fotoPerfil = "";
    private String telefono;
    private String direccion;

    private LocalDate fechaNacimiento;

    private String genero;
    private String dni;

    private LocalDate fechaRegistro;

    private boolean activo;
    private boolean pagado;
}
