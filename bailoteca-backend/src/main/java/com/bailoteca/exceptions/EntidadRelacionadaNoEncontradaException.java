package com.bailoteca.exceptions;

/**
 * Excepción que se lanza cuando no se encuentra una entidad relacionada en la base de datos.
 * Esta excepción es una RuntimeException, lo que significa que no es necesario manejarla explícitamente.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
public class EntidadRelacionadaNoEncontradaException extends RuntimeException {
    public EntidadRelacionadaNoEncontradaException(String entidad, Long id) {
        super("No se encontró la entidad relacionada: " + entidad + " con ID: " + id);
    }
}
