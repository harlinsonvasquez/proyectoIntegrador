package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public final class DocenteDtos {
    private DocenteDtos() {}

    public record OfertaDocenteResponse(Long id, String codigoAsignatura, String asignatura,
            String plan, String periodo, String grupo, long estudiantes) {}

    public record MatriculaDocenteResponse(Long matriculaId, Long estudianteId,
            String codigoEstudiantil, String nombreEstudiante, EstadoMatricula estado,
            BigDecimal notaFinal, String periodo, String grupo) {}

    public record CalificacionDocenteRequest(@NotNull EstadoMatricula estado,
            @PositiveOrZero BigDecimal notaFinal) {}
}
