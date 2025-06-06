package com.bailoteca.dto;

import lombok.Data;

/**
 * DTO para actualizar estado parcial de un usuario (pagado y/o activo).
 * Usado por profesores y admins.
 */
@Data
public class UsuarioUpdateRequest {
    private Boolean pagado;
    private Boolean activo;
}