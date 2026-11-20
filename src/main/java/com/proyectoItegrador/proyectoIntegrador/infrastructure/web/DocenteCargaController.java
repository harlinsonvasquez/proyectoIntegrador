package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.GestionDocenteAcademicaService;
import com.proyectoItegrador.proyectoIntegrador.application.service.GestionEvaluacionesService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DocenteDtos.CalificacionDocenteRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DocenteDtos.MatriculaDocenteResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DocenteDtos.OfertaDocenteResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotaParcialDtos.CrearEvaluacionRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotaParcialDtos.EvaluacionResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotaParcialDtos.NotaParcialDocenteResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotaParcialDtos.RegistrarNotaParcialRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docente")
@RequiredArgsConstructor
@Tag(name = "Docente - Asignaturas", description = "Consulta de ofertas asignadas y gestión de calificaciones.")
@SecurityRequirement(name = "firebaseBearer")
public class DocenteCargaController {
    private final GestionDocenteAcademicaService service;
    private final GestionEvaluacionesService evaluaciones;

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

    @GetMapping("/ofertas/{ofertaId}/evaluaciones")
    @Operation(summary = "Listar evaluaciones de una oferta asignada al docente")
    public List<EvaluacionResponse> evaluaciones(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long ofertaId) {
        return evaluaciones.evaluaciones(actor, ofertaId);
    }

    @PostMapping("/ofertas/{ofertaId}/evaluaciones")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @Operation(summary = "Crear un trabajo o evaluación con porcentaje")
    public EvaluacionResponse crearEvaluacion(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long ofertaId, @Valid @RequestBody CrearEvaluacionRequest request) {
        return evaluaciones.crearEvaluacion(actor, ofertaId, request);
    }

    @GetMapping("/ofertas/{ofertaId}/evaluaciones/{evaluacionId}/notas")
    @Operation(summary = "Listar notas de los estudiantes en una evaluación del docente")
    public List<NotaParcialDocenteResponse> notasDeEvaluacion(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long ofertaId, @PathVariable Long evaluacionId) {
        return evaluaciones.notasDeEvaluacion(actor, ofertaId, evaluacionId);
    }

    @PutMapping("/ofertas/{ofertaId}/evaluaciones/{evaluacionId}/matriculas/{matriculaId}/nota")
    @Operation(summary = "Registrar o actualizar la nota parcial de un estudiante matriculado")
    public NotaParcialDocenteResponse registrarNota(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long ofertaId, @PathVariable Long evaluacionId, @PathVariable Long matriculaId,
            @Valid @RequestBody RegistrarNotaParcialRequest request) {
        return evaluaciones.registrarNota(actor, ofertaId, evaluacionId, matriculaId, request);
    }
}
