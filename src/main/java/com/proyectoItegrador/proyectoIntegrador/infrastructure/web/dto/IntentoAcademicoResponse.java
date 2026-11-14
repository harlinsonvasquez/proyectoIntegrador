package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Una matrícula individual, conservada para no perder repeticiones por periodo. */
public record IntentoAcademicoResponse(
        Long matriculaId,
        Long planAsignaturaId,
        String codigoAsignatura,
        String asignatura,
        String codigoPeriodo,
        String periodo,
        String grupo,
        EstadoMatricula estado,
        BigDecimal notaFinal,
        OffsetDateTime fechaEstado) {
}
