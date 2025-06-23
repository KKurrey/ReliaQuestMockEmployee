package com.reliaquest.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.reliaquest.api.model.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void testHandleEmployeeNotFound() {
        EmployeeNotFoundException ex = new EmployeeNotFoundException("Employee not found");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleEmployeeNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Employee not found", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void testHandleTooManyRequests() {
        TooManyRequestsException ex = new TooManyRequestsException("Too many requests");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleEmployeeTooManyRequest(ex, request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Too many requests", response.getBody().getMessage());
        assertEquals(429, response.getBody().getStatusCode());
    }

    @Test
    void testHandleWebClientError() {
        WebClientResponseException ex = new WebClientResponseException(
                400, "Bad Request", HttpHeaders.EMPTY, "Bad Request Body".getBytes(), StandardCharsets.UTF_8);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleWebClientError(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request Body", response.getBody().getMessage());
    }

    @Test
    void testHandleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("input", "name", "must not be blank");
        FieldError fieldError2 = new FieldError("input", "age", "must be greater than 0");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("name: must not be blank"));
        assertTrue(response.getBody().getMessage().contains("age: must be greater than 0"));
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Something exploded");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(
                "Something went wrong. Please try again later.",
                response.getBody().getMessage());
    }
}
