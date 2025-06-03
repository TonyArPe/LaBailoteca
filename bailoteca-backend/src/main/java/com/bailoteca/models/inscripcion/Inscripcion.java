package com.bailoteca.models.inscripcion;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.enums.EstadoInscripcion;
import com.bailoteca.models.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entidad que representa la inscripción de un usuario a una clase.
 * Relaciona un usuario con una clase e incluye la fecha y el estado de la
 * inscripción.
 */
@Entity
@Table(name = "inscripciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Clase clase;

    private LocalDate fechaInscripcion;

    @Enumerated(EnumType.STRING)
    private EstadoInscripcion estado;
}