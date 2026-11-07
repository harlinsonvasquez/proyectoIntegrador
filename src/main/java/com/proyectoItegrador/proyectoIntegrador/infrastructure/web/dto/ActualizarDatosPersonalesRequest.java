package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarDatosPersonalesRequest(@NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String apellido) {
}
