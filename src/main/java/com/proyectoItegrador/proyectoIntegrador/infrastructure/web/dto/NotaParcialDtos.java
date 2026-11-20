package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class NotaParcialDtos {
    private NotaParcialDtos() {}

    public record CrearEvaluacionRequest(
            @NotBlank @Size(max = 150) String nombre,
            @Size(max = 2000) String descripcion,
            @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2)
            BigDecimal porcentaje,
            OffsetDateTime fechaLimite) {}

    public record RegistrarNotaParcialRequest(
            @NotNull @DecimalMin("0.00") @DecimalMax("5.00") @Digits(integer = 1, fraction = 2)
            BigDecimal nota,
            @Size(max = 1000) String observacion) {}

    public record EvaluacionResponse(Long id, Long ofertaId, String nombre, String descripcion,
            BigDecimal porcentaje, OffsetDateTime fechaLimite, boolean activa) {}

    public record NotaParcialDocenteResponse(Long matriculaId, Long estudianteId,
            String codigoEstudiantil, String nombreEstudiante, String estadoMatricula,
            BigDecimal nota, String observacion, BigDecimal porcentaje, BigDecimal aportePonderado,
            BigDecimal notaFinal) {}

    public record NotaParcialEstudianteResponse(Long notaParcialId, Long matriculaId,
            String codigoAsignatura, String asignatura, String periodo, String grupo,
            String evaluacion, BigDecimal porcentaje, BigDecimal nota, BigDecimal aportePonderado,
            BigDecimal notaFinal, String observacion, OffsetDateTime actualizadaEn) {}
}
