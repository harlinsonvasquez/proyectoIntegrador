package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.ReporteAcademicoService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ReporteDesempenoResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteAcademicoController {
    private final ReporteAcademicoService service;

    @GetMapping("/desempeno")
    public ReporteDesempenoResponse desempeno(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId) { return service.desempeno(actor, programaId); }
    @GetMapping("/participacion")
    public Map<String, Long> participacion(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId) { return service.participacion(actor, programaId); }
}
