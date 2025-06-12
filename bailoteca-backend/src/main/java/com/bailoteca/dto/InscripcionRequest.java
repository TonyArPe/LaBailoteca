package com.bailoteca.dto;

import lombok.Data;

/**
 * Clase que representa una solicitud de inscripción a una clase.
 * Contiene información sobre el usuario y la clase a la que se inscribe.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@Data
public class InscripcionRequest {
    private Long usuarioId;
    private Long claseId;
}
