package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearSolicitudRequest(@NotNull Long matriculaId,
        @NotBlank @Size(max = 2000) String motivo) {
}
