package com.bailoteca.models.clase;

import java.util.List;

import com.bailoteca.models.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.CascadeType;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    // Indica que es el padre de HorarioClase
    @JsonManagedReference
    private List<HorarioClase> horarioClases;

}
