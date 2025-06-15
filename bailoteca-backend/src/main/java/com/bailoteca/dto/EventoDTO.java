package com.bailoteca.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO unificado que representa los datos de un evento en la aplicación.
 * Se utiliza tanto para la creación, edición como para la visualización.
 * 
 * El campo `id` puede ser null en creación, pero obligatorio en edición o respuesta.
 * El campo `nombreOrganizador` es solo informativo (rellenado en respuestas).
 * 
 * @author
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoDTO {

    /**
     * Identificador único del evento (puede ser null al crear).
     */
    private Long id;

    /**
     * Título del evento.
     */
    private String nombre;

    /**
     * Descripción detallada del evento.
     */
    private String descripcion;

    /**
     * Fecha y hora en que se celebrará el evento.
     */
    private String fecha;

    /**
     * Ubicación física o virtual del evento.
     */
    private String lugar;

    /**
     * Indica si el evento es público (visible por invitados).
     */
    private boolean publico;

    /**
     * ID del organizador (se establece en la lógica del backend).
     */
    private Long organizadorId;

    /**
     * Nombre del organizador, útil para mostrar en frontend.
     */
    private String nombreOrganizador;

    /**
     * Ruta o URL de la imagen asociada al evento.
     */
    private String urlImagen;
}