package com.bailoteca.exceptions;

public class EntidadRelacionadaNoEncontradaException extends RuntimeException {
    public EntidadRelacionadaNoEncontradaException(String entidad, Long id) {
        super("No se encontró la entidad relacionada: " + entidad + " con ID: " + id);
    }
}
