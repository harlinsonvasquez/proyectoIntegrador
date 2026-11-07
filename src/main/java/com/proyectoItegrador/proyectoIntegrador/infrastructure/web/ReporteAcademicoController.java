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

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes académicos", description = "Indicadores agregados de desempeño y participación estudiantil.")
@SecurityRequirement(name = "firebaseBearer")
public class ReporteAcademicoController {
    private final ReporteAcademicoService service;

    @GetMapping("/desempeno")
    @Operation(summary = "Reporte de desempeño", description = "Calcula indicadores de notas y estados académicos, opcionalmente por programa.")
    public ReporteDesempenoResponse desempeno(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId) { return service.desempeno(actor, programaId); }
    @GetMapping("/participacion")
    @Operation(summary = "Reporte de participación", description = "Cuenta actividades integrales por categoría, opcionalmente por programa.")
    public Map<String, Long> participacion(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId) { return service.participacion(actor, programaId); }
}
