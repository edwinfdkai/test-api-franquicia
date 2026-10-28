package com.edwin.testapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {

        String message = ex.getMessage() != null ? ex.getMessage() : "Recurso no encontrado";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", 404,
                        "error", "Not Found",
                        "message", message
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Campo inválido",
                        (first, second) -> first
                ));

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", "Hay campos inválidos",
                        "details", errors
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        log.warn("Argumento de petición inválido: {}", ex.getMessage());
        String message = ex.getMessage() != null ? ex.getMessage() : "Argumento inválido";

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", message
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(
            HttpMessageNotReadableException ex) {

        log.warn("Cuerpo de petición malformado o no legible: {}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", "El cuerpo de la petición no es válido o está malformado"
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String paramName = ex.getName() != null ? ex.getName() : "parámetro";
        log.warn("Tipo de parámetro inválido en la ruta: {}", paramName);

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", String.format("El parámetro '%s' tiene un formato o tipo inválido", paramName)
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(
            Exception ex) {

        log.error("Error inesperado en el servidor: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "Ocurrió un error inesperado en el servidor"
                ));
    }
}
