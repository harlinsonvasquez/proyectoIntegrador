package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.ReporteApplicationService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoSolicitud;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ReporteSolicitudesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteApplicationService service;

    @GetMapping("/solicitudes")
    public ReporteSolicitudesResponse solicitudes(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) Long periodoId) {
        return service.solicitudes(actor, estado, programaId, periodoId);
    }
}
