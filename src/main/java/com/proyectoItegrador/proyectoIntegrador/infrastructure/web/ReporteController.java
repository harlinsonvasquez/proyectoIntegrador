package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.ReporteApplicationService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoSolicitud;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ReporteSolicitudesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Indicadores agregados sobre las solicitudes de cancelación.")
@SecurityRequirement(name = "firebaseBearer")
public class ReporteController {
    private final ReporteApplicationService service;

    @GetMapping("/solicitudes")
    @Operation(summary = "Generar reporte de solicitudes", description = "Resume solicitudes y admite filtros opcionales por estado, programa y periodo.")
    public ReporteSolicitudesResponse solicitudes(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) Long periodoId) {
        return service.solicitudes(actor, estado, programaId, periodoId);
    }

    @GetMapping(value = "/solicitudes/csv", produces = "text/csv")
    @Operation(summary = "Exportar reporte de solicitudes a CSV")
    public ResponseEntity<byte[]> exportarSolicitudes(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) Long periodoId) {
        var reporte = service.solicitudes(actor, estado, programaId, periodoId);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("dimension", "valor", "cantidad"));
        rows.add(List.of("total", "solicitudes", String.valueOf(reporte.total())));
        reporte.porEstado().forEach((key, value) -> rows.add(List.of("estado", key, String.valueOf(value))));
        reporte.porPeriodo().forEach((key, value) -> rows.add(List.of("periodo", key, String.valueOf(value))));
        reporte.porPrograma().forEach((key, value) -> rows.add(List.of("programa", key, String.valueOf(value))));
        return CsvReportWriter.attachment("reporte-solicitudes.csv", rows);
    }
}
