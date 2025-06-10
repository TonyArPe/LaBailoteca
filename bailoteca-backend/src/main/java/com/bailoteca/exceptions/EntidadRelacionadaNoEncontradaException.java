package com.bailoteca.exceptions;

/**
 * Excepción personalizada para indicar que una entidad relacionada no fue encontrada.
 * Esta excepción se lanza cuando se intenta acceder a una entidad que
 * no existe en la base de datos o no está relacionada
 * con la entidad actual.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see EntidadRelacionadaNoEncontradaException
 */
public class EntidadRelacionadaNoEncontradaException extends RuntimeException {
    public EntidadRelacionadaNoEncontradaException(String entidad, Long id) {
        super("No se encontró la entidad relacionada: " + entidad + " con ID: " + id);
    }
}
