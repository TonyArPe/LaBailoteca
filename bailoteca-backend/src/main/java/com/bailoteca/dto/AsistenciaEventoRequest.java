package com.bailoteca.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registrar o actualizar la asistencia de un usuario a un evento.
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