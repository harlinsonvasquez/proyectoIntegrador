package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.CategoriaActividad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CrearActividadRequest(@NotNull CategoriaActividad categoria,
        @NotBlank @Size(max = 200) String nombre, @Size(max = 2000) String descripcion,
        @Size(max = 200) String organizacion, LocalDate fechaInicio, LocalDate fechaFin,
        String evidenciaUrl) {
}
