package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.GestionDocenteAcademicaService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DocenteDtos.CalificacionDocenteRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DocenteDtos.MatriculaDocenteResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DocenteDtos.OfertaDocenteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docente")
@RequiredArgsConstructor
@Tag(name = "Docente - Asignaturas", description = "Consulta de ofertas asignadas y gestión de calificaciones.")
@SecurityRequirement(name = "firebaseBearer")
public class DocenteCargaController {
    private final GestionDocenteAcademicaService service;

    @GetMapping("/ofertas")
    @Operation(summary = "Listar asignaturas asignadas al docente")
    public List<OfertaDocenteResponse> ofertas(@AuthenticationPrincipal AuthenticatedUser actor) {
        return service.ofertas(actor);
    }

    @GetMapping("/ofertas/{ofertaId}/matriculas")
    @Operation(summary = "Listar estudiantes matriculados en una oferta del docente")
    public List<MatriculaDocenteResponse> matriculas(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long ofertaId) {
        return service.matriculas(actor, ofertaId);
    }

    @PatchMapping("/matriculas/{matriculaId}/calificacion")
    @Operation(summary = "Actualizar nota final y estado académico de una matrícula propia")
    public MatriculaDocenteResponse actualizarCalificacion(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long matriculaId, @Valid @RequestBody CalificacionDocenteRequest request) {
        return service.actualizarCalificacion(actor, matriculaId, request);
    }
}
