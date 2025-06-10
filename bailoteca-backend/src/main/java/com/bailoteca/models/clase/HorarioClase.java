package com.bailoteca.models.clase;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Modelo que representa un horario de clase en la aplicación.
 * Un horario define el día de la semana y las horas de inicio y fin de una clase.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Clase
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "horarios_clase")
public class HorarioClase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    @ManyToOne
    @JoinColumn(name = "clase_id")
    @JsonBackReference // Indica que es el hijo de Clase
    private Clase clase;

}