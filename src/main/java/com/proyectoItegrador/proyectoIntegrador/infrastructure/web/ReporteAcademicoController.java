package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.ReporteAcademicoService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ReporteDesempenoResponse;
import java.util.Map;
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
@Tag(name = "Reportes académicos", description = "Indicadores agregados de desempeño y participación estudiantil.")
@SecurityRequirement(name = "firebaseBearer")
public class ReporteAcademicoController {
    private final ReporteAcademicoService service;

    @GetMapping("/desempeno")
    @Operation(summary = "Reporte de desempeño", description = "Calcula indicadores de notas y estados académicos, con filtros opcionales por programa y periodo.")
    public ReporteDesempenoResponse desempeno(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) Long periodoId) { return service.desempeno(actor, programaId, periodoId); }

    @GetMapping(value = "/desempeno/csv", produces = "text/csv")
    @Operation(summary = "Exportar reporte de desempeño a CSV")
    public ResponseEntity<byte[]> exportarDesempeno(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) Long periodoId) {
        var reporte = service.desempeno(actor, programaId, periodoId);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("indicador", "valor"));
        rows.add(List.of("totalMatriculas", String.valueOf(reporte.totalMatriculas())));
        rows.add(List.of("aprobadas", String.valueOf(reporte.aprobadas())));
        rows.add(List.of("reprobadas", String.valueOf(reporte.reprobadas())));
        rows.add(List.of("enCurso", String.valueOf(reporte.enCurso())));
        rows.add(List.of("promedioNotas", reporte.promedioNotas() == null ? "" : reporte.promedioNotas().toPlainString()));
        return CsvReportWriter.attachment("reporte-desempeno.csv", rows);
    }
    @GetMapping("/participacion")
    @Operation(summary = "Reporte de participación", description = "Cuenta actividades integrales por categoría, con filtros opcionales por programa y periodo. El periodo se determina por solapamiento de fechas.")
    public Map<String, Long> participacion(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) Long periodoId) {
        return service.participacion(actor, programaId, periodoId);
    }

    @GetMapping(value = "/participacion/csv", produces = "text/csv")
    @Operation(summary = "Exportar reporte de participación a CSV")
    public ResponseEntity<byte[]> exportarParticipacion(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) Long periodoId) {
        Map<String, Long> reporte = service.participacion(actor, programaId, periodoId);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("categoria", "cantidad"));
        reporte.forEach((key, value) -> rows.add(List.of(key, String.valueOf(value))));
        return CsvReportWriter.attachment("reporte-participacion.csv", rows);
    }
}
