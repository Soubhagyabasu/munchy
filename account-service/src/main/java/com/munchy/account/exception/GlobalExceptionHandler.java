package com.munchy.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidSessionException.class)
    ResponseEntity<ApiError> unauthorized(InvalidSessionException exception) {
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    ResponseEntity<ApiError> validation(WebExchangeBindException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getFieldErrors().forEach(fieldError ->
                errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> badRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String message,
            Map<String, String> validationErrors) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), message, validationErrors));
    }
}
