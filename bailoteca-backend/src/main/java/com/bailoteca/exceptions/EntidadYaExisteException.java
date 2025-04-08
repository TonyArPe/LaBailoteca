package com.bailoteca.exceptions;

public class EntidadYaExisteException extends RuntimeException {
    public EntidadYaExisteException(String mensaje) {
        super(mensaje);
    }
}
