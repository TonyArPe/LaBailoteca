package com.bailoteca.dto;

import lombok.Data;

/**
 * DTO para registrar una inscripción desde el cliente móvil o web.
 * Este objeto se utiliza para enviar datos desde el cliente al servidor
 * para registrar la inscripción de un usuario a una clase específica.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see InscripcionRequest
 * @see InscripcionService
 * @see InscripcionRepo
 */
@Data
public class InscripcionRequest {
    private Long usuarioId;
    private Long claseId;
}
