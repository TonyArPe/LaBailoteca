package com.bailoteca.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa una solicitud de asistencia a un evento.
 * Contiene información sobre si el usuario asistirá y si ha pagado el evento.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class AsistenciaEventoRequest {

    /**
     * Indica si el usuario asistirá al evento.
     */
    private boolean asistira;

    /**
     * Indica si el usuario ha pagado el evento.
     */
    private boolean pagado;
}