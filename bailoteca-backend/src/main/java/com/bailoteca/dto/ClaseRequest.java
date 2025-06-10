package com.bailoteca.dto;

import com.bailoteca.models.enums.Dificultad;
import lombok.Data;

import java.util.List;

/**
 * DTO para recibir datos desde el frontend al crear o actualizar una clase.
 * Este DTO se utiliza tanto para creación como para actualización parcial.
 * El campo `publica` es Boolean (objeto) para permitir valores nulos en actualizaciones.
 * Si se envía como null, se mantendrá el valor actual de la clase.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Dificultad
 * @see HorarioClaseRequest
 * @see ClaseRequest
 * @see ClaseService
 * @see ClaseRepo
 * @see Usuario
 * @see UsuarioRepo
 * @see UsuarioDetails
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