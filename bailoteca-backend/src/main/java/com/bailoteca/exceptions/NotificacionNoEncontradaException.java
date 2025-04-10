package com.bailoteca.exceptions;

public class NotificacionNoEncontradaException extends RuntimeException {
    public NotificacionNoEncontradaException(Long id) {
        super("Notificación no encontrada con ID: " + id);
    }
}
