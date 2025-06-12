package com.bailoteca.exceptions;

import java.time.LocalDateTime;

/**
 * Clase que representa una respuesta de error para las excepciones personalizadas.
 * Contiene información sobre el estado, error, mensaje y la marca de tiempo del error.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

