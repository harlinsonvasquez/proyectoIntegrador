package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.AdministracionCrudService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.ActividadAdminResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.ActividadRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/actividades")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
@Tag(name = "Administración - Actividades", description = "Administra actividades integrales asociadas a estudiantes.")
@SecurityRequirement(name = "firebaseBearer")
public class AdministracionActividadController {
    private final AdministracionCrudService service;

    @GetMapping
    @Operation(summary = "Listar actividades", description = "Lista actividades y permite filtrar opcionalmente por estudianteId.")
    public List<ActividadAdminResponse> actividades(@RequestParam(required = false) Long estudianteId) {
        return service.actividades(estudianteId);
    }
    @GetMapping("/{id}")
    @Operation(summary = "Consultar actividad", description = "Obtiene el detalle de una actividad por su ID.")
    public ActividadAdminResponse actividad(@PathVariable Long id) { return service.actividad(id); }
    @PostMapping
    @Operation(summary = "Registrar actividad", description = "Registra una actividad integral para un estudiante.")
    @ResponseStatus(HttpStatus.CREATED)
    public ActividadAdminResponse crear(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody ActividadRequest r) { return service.crearActividad(actor, r); }
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar actividad", description = "Actualiza los datos de la actividad sin cambiar el estudiante asociado.")
    public ActividadAdminResponse actualizar(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody ActividadRequest r) {
        return service.actualizarActividad(actor, id, r);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar actividad", description = "Elimina la actividad indicada.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        service.eliminarActividad(actor, id);
    }
}
