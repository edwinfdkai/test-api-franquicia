package com.edwin.testapi.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFound - should return 404 with error details")
    void handleNotFound_ShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado con id: 5");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("Recurso no encontrado con id: 5");
    }

    @Test
    @DisplayName("handleNotFound - should handle null message safely")
    void handleNotFound_WhenMessageIsNull_ShouldUseDefault() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null);

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).isEqualTo("Recurso no encontrado");
    }

    @Test
    @DisplayName("handleValidation - should return 400 with field errors")
    void handleValidation_ShouldReturn400WithDetails() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "name", "El nombre es obligatorio");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Hay campos inválidos");

        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertThat(details).containsEntry("name", "El nombre es obligatorio");
    }

    @Test
    @DisplayName("handleIllegalArgument - should return 400 with client error message")
    void handleIllegalArgument_ShouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("El stock debe ser mayor o igual a cero");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("El stock debe ser mayor o igual a cero");
    }

    @Test
    @DisplayName("handleNotReadable - should return 400 with user-friendly message for malformed JSON")
    void handleNotReadable_ShouldReturn400() {
        @SuppressWarnings("deprecation")
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error: Unexpected character");

        ResponseEntity<Map<String, Object>> response = handler.handleNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("El cuerpo de la petición no es válido o está malformado");
        assertThat(response.getBody().toString()).doesNotContain("JSON parse error");
    }

    @Test
    @DisplayName("handleTypeMismatch - should return 400 with parameter name")
    void handleTypeMismatch_ShouldReturn400() throws NoSuchMethodException {
        Method method = String.class.getMethod("valueOf", int.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "id", parameter, new NumberFormatException()
        );

        ResponseEntity<Map<String, Object>> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("El parámetro 'id' tiene un formato o tipo inválido");
    }

    @Test
    @DisplayName("handleUnexpected - should return 500 without leaking stack traces or internal messages")
    void handleUnexpected_ShouldReturn500WithoutLeakingInternals() {
        Exception ex = new RuntimeException("Sensitive database connection credentials or query syntax error");

        ResponseEntity<Map<String, Object>> response = handler.handleUnexpected(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("Ocurrió un error inesperado en el servidor");
        assertThat(response.getBody().toString()).doesNotContain("Sensitive");
    }
}
