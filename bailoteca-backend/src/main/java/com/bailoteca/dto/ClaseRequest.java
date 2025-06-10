package com.bailoteca.dto;

import com.bailoteca.models.enums.Dificultad;
import lombok.Data;

import java.util.List;

/**
 * Clase que representa una solicitud para crear o actualizar una clase de baile.
 * Contiene información sobre el profesor, nombre, descripción, ubicación,
 * video de presentación, dificultad, visibilidad y horarios de la clase.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@Data
public class ClaseRequest {
    private Long profesorId;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String videoPresentacion;
    private Dificultad dificultad;
    private Boolean publica;
    private List<HorarioClaseRequest> horarioClases;
}