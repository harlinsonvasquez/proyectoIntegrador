package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import java.math.BigDecimal;

public record ReporteDesempenoResponse(long totalMatriculas, long aprobadas,
        long reprobadas, long enCurso, BigDecimal promedioNotas) {
}
