package com.bailoteca.exceptions;

/**
 * Excepción personalizada para indicar que un recurso no fue encontrado.
 * Esta excepción se lanza cuando se intenta acceder a un recurso
 * que no existe en la base de datos o no está disponible.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see RecursoNoEncontradoException
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
