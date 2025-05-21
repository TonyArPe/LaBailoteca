package com.bailoteca.models.clase;

import java.util.ArrayList;
import java.util.List;

import com.bailoteca.models.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

import com.bailoteca.models.enums.Dificultad;

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

    @ManyToOne
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor;

    private String ubicacion;

    private Dificultad dificultad;

    private String videoPresentacion;

    @OneToMany(mappedBy = "clase", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<HorarioClase> horarioClases = new ArrayList<>();

    @Column(nullable = false)
    private boolean publica = false;
}