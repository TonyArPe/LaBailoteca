package com.bailoteca.exceptions;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.persistence.EntityNotFoundException;



/**
 * Manejador global de excepciones para la aplicación.
 * Se encarga de manejar las excepciones que no son capturadas por los controladores específicos.
 * Puede incluir métodos para manejar excepciones específicas
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de tipo entidad no encontrada
     * @param ex Excepción lanzada
     * @return Mensaje de error personalizado
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>("Entidad no encontrada: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Manejador de excepciones de datos vacios
     * @param ex Excepción lanzada
     * @return Mensaje de error personalizado
     */
    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex){
            return new ResponseEntity<>("No se encontró el evento: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Manejador de excepciones generales
     * @param ex Excepción lanzada
     * @return Mensaje de error personalizado
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return new ResponseEntity<>("Error interno del servidor: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}
