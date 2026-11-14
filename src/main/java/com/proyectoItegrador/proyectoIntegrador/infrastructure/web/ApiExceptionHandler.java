package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.ReglaNegocioException;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<?> regla(ReglaNegocioException exception) {
        return ResponseEntity.status(exception.getStatus()).body(Map.of(
                "timestamp", OffsetDateTime.now(), "status", exception.getStatus().value(),
                "error", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validacion(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("campo", e.getField(), "mensaje", e.getDefaultMessage())).toList();
        return ResponseEntity.badRequest().body(Map.of("timestamp", OffsetDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(), "errores", errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflicto(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", OffsetDateTime.now(), "status", HttpStatus.CONFLICT.value(),
                "error", "La operación infringe una regla de integridad o ya existe una solicitud activa."));
    }
}
