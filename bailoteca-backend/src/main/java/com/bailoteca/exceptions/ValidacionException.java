package com.bailoteca.exceptions;

/**
 * Excepción que se lanza cuando hay un error de validación en los datos proporcionados.
 * Esta excepción es una RuntimeException, lo que significa que no es necesario manejarla explícitamente.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
