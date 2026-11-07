package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.BusquedaEstudianteService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoEstudiante;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.EstudianteResumenResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docente/estudiantes")
@RequiredArgsConstructor
public class BusquedaEstudianteController {
    private final BusquedaEstudianteService service;

    @GetMapping
    public List<EstudianteResumenResponse> buscar(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) EstadoEstudiante estado,
            @RequestParam(required = false) String q) {
        return service.buscar(actor, programaId, estado, q);
    }
}
