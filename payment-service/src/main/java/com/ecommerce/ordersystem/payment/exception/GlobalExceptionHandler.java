package com.ecommerce.ordersystem.payment.exception;

import com.ecommerce.ordersystem.common.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                UUID.randomUUID().toString(),
                Instant.now(),
                List.of(ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed",
                UUID.randomUUID().toString(),
                Instant.now(),
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception in payment-service", ex);
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred in payment-service",
                UUID.randomUUID().toString(),
                Instant.now(),
                List.of(ex.getMessage() != null ? ex.getMessage() : "Unknown error")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
