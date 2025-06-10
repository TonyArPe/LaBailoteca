package com.bailoteca.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.ConstraintViolationException;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación.
 * Captura y maneja las excepciones lanzadas por los controladores,
 * proporcionando respuestas de error personalizadas.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    /**
     * Maneja la excepción de entidad ya existente.
     *
     * @param ex la excepción de entidad ya existente
     * @return la respuesta de error personalizada
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Entidad no encontrada", ex.getMessage());
    }

    /**
     * Maneja la excepción de acceso no autorizado.
     *
     * @param ex la excepción de acceso no autorizado
     * @return la respuesta de error personalizada
     */
    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "No se encontró el elemento solicitado",
                ex.getMessage());
    }

    /**
     * Maneja la excepción de validación de datos.
     *
     * @param ex la excepción de validación de datos
     * @return la respuesta de error personalizada
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException ex) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Error de validación", ex.getMessage());
    }

    /**
     * Maneja la excepción de acceso denegado.
     *
     * @param ex la excepción de acceso denegado
     * @return la respuesta de error personalizada
     */
    @ExceptionHandler(AccessDeniedException.class)

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Acceso denegado",
                ex.getMessage());
    }

    /**
     * Maneja la excepción de validación de argumentos del método.
     *
     * @param ex la excepción de validación de argumentos del método
     * @return la respuesta de error personalizada
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : result.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Errores de validación",
                fieldErrors.toString());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja la excepción de entidad ya existente.
     *
     * @param ex la excepción de entidad ya existente
     * @return la respuesta de error personalizada
     */
    @ExceptionHandler(NotificacionNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String manejarNotificacionNoEncontrada(NotificacionNoEncontradaException ex) {
        return ex.getMessage();
    }

    /**
     * Maneja la excepción de entidad relacionada no encontrada.
     *
     * @param ex la excepción de entidad relacionada no encontrada
     * @return la respuesta de error personalizada
     */
    @ExceptionHandler(EntidadRelacionadaNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntidadRelacionadaNoEncontrada(EntidadRelacionadaNoEncontradaException ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Entidad relacionada no encontrada", ex.getMessage());
    }

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja cualquier otra excepción no controlada.
     *
     * @param ex la excepción no controlada
     * @return la respuesta de error personalizada
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneralException(Exception ex) {
        logger.error("Error inesperado", ex);
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno del servidor",
                ex.getMessage());
    }
}