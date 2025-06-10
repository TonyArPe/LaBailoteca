package com.bailoteca.exceptions;

/**
 * Excepción que se lanza cuando no se encuentra una notificación en la base de datos.
 * Esta excepción es una RuntimeException, lo que significa que no es necesario manejarla explícitamente.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
public class NotificacionNoEncontradaException extends RuntimeException {
    public NotificacionNoEncontradaException(Long id) {
        super("Notificación no encontrada con ID: " + id);
    }
}
