package com.bailoteca.exceptions;

import java.time.LocalDateTime;

/**
 * Clase que representa la estructura de una respuesta de error personalizada.
 * Esta clase se utiliza para enviar información detallada sobre errores
 * que ocurren en la aplicación, incluyendo el estado HTTP, el tipo de error,
 * un mensaje descriptivo y una marca de tiempo.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see LocalDateTime
 * @see ErrorResponse
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

