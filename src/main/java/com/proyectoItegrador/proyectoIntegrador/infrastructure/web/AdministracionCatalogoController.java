package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.AdministracionCrudService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
@Tag(name = "Administración - Catálogos", description = "Gestión de programas, planes, asignaturas y sus relaciones.")
@SecurityRequirement(name = "firebaseBearer")
public class AdministracionCatalogoController {
    private final AdministracionCrudService service;

    @GetMapping("/programas")
    @Operation(summary = "Listar programas", description = "Devuelve todos los programas académicos.")
    public List<ProgramaResponse> programas() { return service.programas(); }
    @GetMapping("/programas/{id}")
    @Operation(summary = "Consultar programa", description = "Obtiene el detalle de un programa por su ID.")
    public ProgramaResponse programa(@PathVariable Long id) { return service.programa(id); }
    @PostMapping("/programas")
    @Operation(summary = "Crear programa", description = "Registra un programa académico.")
    @ResponseStatus(HttpStatus.CREATED)
    public ProgramaResponse crearPrograma(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody ProgramaRequest r) { return service.crearPrograma(actor, r); }
    @PutMapping("/programas/{id}")
    @Operation(summary = "Actualizar programa", description = "Actualiza los datos y estado activo del programa.")
    public ProgramaResponse actualizarPrograma(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody ProgramaRequest r) {
        return service.actualizarPrograma(actor, id, r);
    }
    @DeleteMapping("/programas/{id}")
    @Operation(summary = "Desactivar programa", description = "Realiza una baja lógica; conserva el historial relacionado.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPrograma(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        service.eliminarPrograma(actor, id);
    }

    @GetMapping("/planes-estudio")
    @Operation(summary = "Listar planes de estudio", description = "Devuelve todos los planes académicos.")
    public List<PlanResponse> planes() { return service.planes(); }
    @GetMapping("/planes-estudio/{id}")
    @Operation(summary = "Consultar plan de estudio", description = "Obtiene un plan por su ID.")
    public PlanResponse plan(@PathVariable Long id) { return service.plan(id); }
    @PostMapping("/planes-estudio")
    @Operation(summary = "Crear plan de estudio", description = "Registra un plan asociado a un programa.")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse crearPlan(@AuthenticationPrincipal AuthenticatedUser actor, @Valid @RequestBody PlanRequest r) {
        return service.crearPlan(actor, r);
    }
    @PutMapping("/planes-estudio/{id}")
    @Operation(summary = "Actualizar plan de estudio", description = "Modifica la información y vigencia del plan.")
    public PlanResponse actualizarPlan(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id,
            @Valid @RequestBody PlanRequest r) { return service.actualizarPlan(actor, id, r); }
    @DeleteMapping("/planes-estudio/{id}")
    @Operation(summary = "Desactivar plan de estudio", description = "Realiza una baja lógica y conserva los registros académicos.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPlan(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        service.eliminarPlan(actor, id);
    }

    @GetMapping("/asignaturas")
    @Operation(summary = "Listar asignaturas", description = "Devuelve el catálogo de asignaturas.")
    public List<AsignaturaResponse> asignaturas() { return service.asignaturas(); }
    @GetMapping("/asignaturas/{id}")
    @Operation(summary = "Consultar asignatura", description = "Obtiene una asignatura por su ID.")
    public AsignaturaResponse asignatura(@PathVariable Long id) { return service.asignatura(id); }
    @PostMapping("/asignaturas")
    @Operation(summary = "Crear asignatura", description = "Agrega una asignatura al catálogo.")
    @ResponseStatus(HttpStatus.CREATED)
    public AsignaturaResponse crearAsignatura(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody AsignaturaRequest r) { return service.crearAsignatura(actor, r); }
    @PutMapping("/asignaturas/{id}")
    @Operation(summary = "Actualizar asignatura", description = "Modifica los datos de una asignatura.")
    public AsignaturaResponse actualizarAsignatura(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody AsignaturaRequest r) {
        return service.actualizarAsignatura(actor, id, r);
    }
    @DeleteMapping("/asignaturas/{id}")
    @Operation(summary = "Desactivar asignatura", description = "Realiza una baja lógica para no borrar el historial.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarAsignatura(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        service.eliminarAsignatura(actor, id);
    }

    @GetMapping("/plan-asignaturas")
    @Operation(summary = "Listar asignaturas de planes", description = "Devuelve las asignaturas vinculadas a sus planes.")
    public List<PlanAsignaturaResponse> planAsignaturas() { return service.planAsignaturas(); }
    @GetMapping("/plan-asignaturas/{id}")
    @Operation(summary = "Consultar asignatura de plan", description = "Obtiene el vínculo entre plan y asignatura.")
    public PlanAsignaturaResponse planAsignatura(@PathVariable Long id) { return service.planAsignatura(id); }
    @PostMapping("/plan-asignaturas")
    @Operation(summary = "Agregar asignatura a plan", description = "Crea la relación curricular indicando semestre y obligatoriedad.")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanAsignaturaResponse crearPlanAsignatura(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody PlanAsignaturaRequest r) { return service.crearPlanAsignatura(actor, r); }
    @PutMapping("/plan-asignaturas/{id}")
    @Operation(summary = "Actualizar asignatura de plan", description = "Modifica los datos curriculares del vínculo.")
    public PlanAsignaturaResponse actualizarPlanAsignatura(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody PlanAsignaturaRequest r) {
        return service.actualizarPlanAsignatura(actor, id, r);
    }
    @DeleteMapping("/plan-asignaturas/{id}")
    @Operation(summary = "Eliminar asignatura de plan", description = "Elimina la relación curricular si no tiene dependencias.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPlanAsignatura(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        service.eliminarPlanAsignatura(actor, id);
    }

    @GetMapping("/prerrequisitos")
    @Operation(summary = "Listar prerrequisitos", description = "Devuelve las relaciones de prerrequisito entre asignaturas.")
    public List<PrerrequisitoResponse> prerrequisitos() { return service.prerrequisitos(); }
    @GetMapping("/prerrequisitos/{planId}/{requisitoId}")
    @Operation(summary = "Consultar prerrequisito", description = "Busca la relación por ID de asignatura del plan y prerrequisito.")
    public PrerrequisitoResponse prerrequisito(@PathVariable Long planId, @PathVariable Long requisitoId) {
        return service.prerrequisito(planId, requisitoId);
    }
    @PostMapping("/prerrequisitos")
    @Operation(summary = "Crear prerrequisito", description = "Asocia un prerrequisito a una asignatura del plan; no permite autorreferencias.")
    @ResponseStatus(HttpStatus.CREATED)
    public PrerrequisitoResponse crearPrerrequisito(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody PrerrequisitoRequest r) { return service.crearPrerrequisito(actor, r); }
    @DeleteMapping("/prerrequisitos/{planId}/{requisitoId}")
    @Operation(summary = "Eliminar prerrequisito", description = "Elimina la relación de prerrequisito indicada.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPrerrequisito(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long planId, @PathVariable Long requisitoId) {
        service.eliminarPrerrequisito(actor, planId, requisitoId);
    }
}
