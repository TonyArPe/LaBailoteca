package com.bailoteca.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferir los datos de un evento al frontend.
 * Contiene solo los campos necesarios para visualización.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fecha;
    private String lugar;
    private boolean publico;
    private String estado;
    private String organizadorNombre;
}