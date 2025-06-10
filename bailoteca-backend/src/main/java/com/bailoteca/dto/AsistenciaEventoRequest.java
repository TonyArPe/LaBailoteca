package com.bailoteca.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registrar o actualizar la asistencia de un usuario a un evento.
 * Contiene información sobre si el usuario asistirá al evento y si ha pagado.
 * Este objeto se utiliza para enviar datos desde el cliente al servidor
 * para registrar la asistencia a un evento específico.
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see AsistenciaEvento
 * @see AsistenciaEventoService
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