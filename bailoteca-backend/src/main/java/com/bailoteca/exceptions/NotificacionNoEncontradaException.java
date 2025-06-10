package com.bailoteca.exceptions;

/**
 * Excepción personalizada para indicar que una notificación no fue encontrada.
 * Esta excepción se lanza cuando se intenta acceder a una notificación
 * que no existe en la base de datos.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see NotificacionNoEncontradaException
 */
public class NotificacionNoEncontradaException extends RuntimeException {
    public NotificacionNoEncontradaException(Long id) {
        super("Notificación no encontrada con ID: " + id);
    }
}
