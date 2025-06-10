package com.bailoteca.exceptions;

/**
 * Excepción personalizada para indicar que una operación no está permitida.
 * Esta excepción se lanza cuando se intenta realizar
 * una operación que no es válida o no está permitida en el contexto actual.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see OperacionNoPermitidaException
 */
public class OperacionNoPermitidaException extends RuntimeException {
    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
