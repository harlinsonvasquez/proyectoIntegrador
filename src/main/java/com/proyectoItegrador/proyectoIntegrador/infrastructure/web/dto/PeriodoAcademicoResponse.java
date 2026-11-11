package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import java.time.LocalDate;

public record PeriodoAcademicoResponse(Long id, String codigo, String nombre,
        LocalDate fechaInicio, LocalDate fechaFin) {
}
