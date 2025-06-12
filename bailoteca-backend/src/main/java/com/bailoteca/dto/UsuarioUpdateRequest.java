package com.bailoteca.dto;

import lombok.Data;

/**
 * Clase que representa una solicitud para actualizar un usuario.
 * Contiene información sobre el estado de pago y actividad del usuario.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@Data
public class UsuarioUpdateRequest {
    private Boolean pagado;
    private Boolean activo;
}