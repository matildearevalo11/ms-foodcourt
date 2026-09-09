package com.pragma.powerup.infrastructure.exceptionhandler;

import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.AuthorizationException;
import com.pragma.powerup.domain.exception.AuthenticationException;
import com.pragma.powerup.domain.exception.ValidationException;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvisor {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> invalidRequest(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Collections.singletonMap("errors", fields));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Map<String, Object>> invalidParameter(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(errorBody(exception.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    ResponseEntity<Map<String, Object>> businessValidation(ValidationException exception) {
        return ResponseEntity.badRequest().body(errorBody(exception.getMessage()));
    }

    @ExceptionHandler(ExternalServiceException.class)
    ResponseEntity<Map<String, Object>> externalService(ExternalServiceException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorBody(exception.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Map<String, Object>> notFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(exception.getMessage()));
    }

    @ExceptionHandler(AuthorizationException.class)
    ResponseEntity<Map<String, Object>> forbidden(AuthorizationException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(exception.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String, Object>> unauthorized(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(exception.getMessage()));
    }

    private Map<String, Object> errorBody(String message) {
        return Collections.singletonMap("errors", Collections.singletonMap("message", message));
    }
}
