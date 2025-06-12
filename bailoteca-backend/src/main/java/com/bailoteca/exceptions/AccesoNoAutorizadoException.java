package com.bailoteca.exceptions;

/**
 * Excepción que se lanza cuando un usuario intenta acceder a un recurso sin la autorización adecuada.
 * Esta excepción es una RuntimeException, lo que significa que no es necesario manejarla explícitamente.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
public class AccesoNoAutorizadoException extends RuntimeException {
    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
