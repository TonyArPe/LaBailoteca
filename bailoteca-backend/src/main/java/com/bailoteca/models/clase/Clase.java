package com.bailoteca.models.clase;

import java.util.ArrayList;
import java.util.List;

import com.bailoteca.models.enums.Dificultad;
import com.bailoteca.models.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

/**
 * Modelo que representa una clase de baile en la aplicación.
 * Una clase puede tener un profesor, horarios y puede ser pública o privada.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Usuario
 * @see HorarioClase
 * @see Dificultad
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clases")
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private Dificultad dificultad;

    private String ubicacion;

    private String videoPresentacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor;

    // Relación con horarios de clase. Cada clase puede tener múltiples horarios.
    @OneToMany(mappedBy = "clase", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<HorarioClase> horarioClases = new ArrayList<>();

    @Column(nullable = false)
    private boolean publica = false;
}