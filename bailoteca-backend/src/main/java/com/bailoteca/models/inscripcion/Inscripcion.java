package com.bailoteca.models.inscripcion;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.enums.EstadoInscripcion;
import com.bailoteca.models.usuario.Usuario;

/**
 * Entidad que representa la inscripción de un usuario a una clase.
 * Relaciona un usuario con una clase e incluye la fecha y el estado de la inscripción.
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

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "clase_id", nullable = false)
    private Clase clase;
    
    private LocalDate fechaInscripcion;

    @Enumerated(EnumType.STRING)
    private EstadoInscripcion estado;
}