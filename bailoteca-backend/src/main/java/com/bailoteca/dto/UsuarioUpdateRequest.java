package com.bailoteca.dto;

import lombok.Data;

/**
 * DTO para actualizar estado parcial de un usuario (pagado y/o activo).
 * Usado por profesores y admins.
 * Este objeto se utiliza para enviar datos desde el cliente al servidor
 * para actualizar el estado de un usuario, indicando si ha pagado
 * y si está activo.
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see UsuarioUpdateRequest
 * @see Usuario
 */
@Data
public class UsuarioUpdateRequest {
    private Boolean pagado;
    private Boolean activo;
}