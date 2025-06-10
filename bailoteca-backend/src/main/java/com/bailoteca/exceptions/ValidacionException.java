package com.bailoteca.exceptions;

/**
 * Excepción personalizada para indicar que una validación ha fallado.
 * Esta excepción se lanza cuando los datos proporcionados no cumplen
 * con los criterios de validación establecidos.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see ValidacionException
 */
public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
