package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import java.util.Map;

public record ReporteSolicitudesResponse(long total,
        Map<String, Long> porEstado, Map<String, Long> porPeriodo, Map<String, Long> porPrograma) {
}
