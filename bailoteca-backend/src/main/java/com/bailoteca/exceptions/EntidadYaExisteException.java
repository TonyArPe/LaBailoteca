package com.bailoteca.exceptions;

/**
 * Excepción personalizada para indicar que una entidad ya existe.
 * Esta excepción se lanza cuando se intenta crear
 * una entidad que ya está presente en la base de datos.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see EntidadYaExisteException
 */
public class EntidadYaExisteException extends RuntimeException {
    public EntidadYaExisteException(String mensaje) {
        super(mensaje);
    }
}
