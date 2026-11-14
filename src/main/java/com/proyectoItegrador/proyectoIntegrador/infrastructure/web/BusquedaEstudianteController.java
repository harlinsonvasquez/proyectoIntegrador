package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.BusquedaEstudianteService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoEstudiante;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.EstudianteResumenResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.ProgramaResponse;
import java.util.List;
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
@RequestMapping("/api/docente")
@RequiredArgsConstructor
@Tag(name = "Consulta docente de estudiantes", description = "Búsqueda de estudiantes para consulta académica docente.")
@SecurityRequirement(name = "firebaseBearer")
public class BusquedaEstudianteController {
    private final BusquedaEstudianteService service;

    @GetMapping("/programas")
    @Operation(summary = "Listar programas del docente", description = "Devuelve programas con ofertas asignadas al docente.")
    public List<ProgramaResponse> programas(@AuthenticationPrincipal AuthenticatedUser actor) {
        return service.programas(actor);
    }

    @GetMapping("/estudiantes")
    @Operation(summary = "Buscar estudiantes", description = "Filtra estudiantes por programa, estado y texto de búsqueda.")
    public List<EstudianteResumenResponse> buscar(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) EstadoEstudiante estado,
            @RequestParam(required = false) String q) {
        return service.buscar(actor, programaId, estado, q);
    }
}
