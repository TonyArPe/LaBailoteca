package com.bailoteca.dto;

import com.bailoteca.models.enums.Dificultad;
import lombok.Data;

/**
 * DTO para recibir datos desde el frontend al crear o actualizar una clase.
 * No incluye el profesor, ya que se asigna automáticamente con el usuario autenticado.
 */
@Data
public class ClaseRequest {
    private String nombre;
    private String descripcion;
    private String videoPresentacion;
    private Dificultad dificultad;
    private boolean publica;
}
