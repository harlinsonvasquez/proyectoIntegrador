package com.proyectoItegrador.proyectoIntegrador.application.service;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ReglaNegocioException extends RuntimeException {
    @Getter
    private final HttpStatus status;
    public ReglaNegocioException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
