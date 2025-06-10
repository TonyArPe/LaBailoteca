package com.bailoteca.exceptions;

/**
 * Excepción personalizada para indicar que un acceso no está autorizado.
 * Esta excepción se lanza cuando un usuario intenta acceder a un recurso
 * o realizar una acción para la cual no tiene permisos adecuados.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see AccesoNoAutorizadoException
 */
public class AccesoNoAutorizadoException extends RuntimeException {
    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
