package com.bailoteca.dto;

import lombok.Data;

/**
 * DTO para registrar una inscripción desde el cliente móvil o web.
 */
@Data
public class InscripcionRequest {
    private Long usuarioId;
    private Long claseId;
}
