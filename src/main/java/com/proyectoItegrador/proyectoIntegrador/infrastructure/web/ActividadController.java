package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.ActividadApplicationService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActividadResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.CrearActividadRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Actividades estudiantiles", description = "Registro y consulta de actividades integrales de estudiantes.")
@SecurityRequirement(name = "firebaseBearer")
public class ActividadController {
    private final ActividadApplicationService service;

    @GetMapping("/estudiantes/me/actividades")
    @Operation(summary = "Listar mis actividades", description = "Devuelve las actividades integrales del estudiante autenticado.")
    public List<ActividadResponse> mias(@AuthenticationPrincipal AuthenticatedUser actor) { return service.mias(actor); }
    @PostMapping("/estudiantes/me/actividades")
    @Operation(summary = "Registrar mi actividad", description = "Registra una actividad integral a nombre del estudiante autenticado.")
    @ResponseStatus(HttpStatus.CREATED)
    public ActividadResponse registrar(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody CrearActividadRequest request) { return service.registrar(actor, request); }
    @GetMapping("/docente/estudiantes/{id}/actividades")
    @Operation(summary = "Consultar actividades de estudiante", description = "Permite a un docente autorizado consultar las actividades del estudiante indicado.")
    public List<ActividadResponse> deEstudiante(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id) { return service.deEstudiante(actor, id); }
}
