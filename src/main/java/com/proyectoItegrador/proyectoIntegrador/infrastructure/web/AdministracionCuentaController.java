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
@Tag(name = "Administración - Cuentas y perfiles", description = "Administra estudiantes, docentes y perfiles sociodemográficos.")
@SecurityRequirement(name = "firebaseBearer")
public class AdministracionCuentaController {
    private final AdministracionCrudService service;

    @GetMapping("/estudiantes")
    @Operation(summary = "Listar estudiantes", description = "Devuelve estudiantes junto con sus datos locales de usuario.")
    public List<EstudianteResponse> estudiantes() { return service.estudiantes(); }
    @GetMapping("/estudiantes/{id}")
    @Operation(summary = "Consultar estudiante", description = "Obtiene el detalle de un estudiante por su ID.")
    public EstudianteResponse estudiante(@PathVariable Long id) { return service.estudiante(id); }
    @PostMapping("/estudiantes")
    @Operation(summary = "Crear estudiante", description = "Crea la cuenta en Firebase Authentication y el perfil local; la contraseña no se almacena en la base de datos.")
    @ResponseStatus(HttpStatus.CREATED)
    public EstudianteResponse crearEstudiante(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody CrearEstudianteRequest r) { return service.crearEstudiante(actor, r); }
    @PutMapping("/estudiantes/{id}")
    @Operation(summary = "Actualizar estudiante", description = "Modifica datos académicos y personales y sincroniza su estado activo con Firebase.")
    public EstudianteResponse actualizarEstudiante(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody ActualizarEstudianteRequest r) {
        return service.actualizarEstudiante(actor, id, r);
    }
    @DeleteMapping("/estudiantes/{id}")
    @Operation(summary = "Inactivar estudiante", description = "Inactiva el perfil y deshabilita su acceso en Firebase; no borra el historial.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarEstudiante(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        service.eliminarEstudiante(actor, id);
    }

    @GetMapping("/docentes")
    @Operation(summary = "Listar docentes", description = "Devuelve los docentes registrados.")
    public List<DocenteResponse> docentes() { return service.docentes(); }
    @GetMapping("/docentes/{id}")
    @Operation(summary = "Consultar docente", description = "Obtiene el detalle de un docente por su ID.")
    public DocenteResponse docente(@PathVariable Long id) { return service.docente(id); }
    @PostMapping("/docentes")
    @Operation(summary = "Crear docente", description = "Crea la cuenta en Firebase Authentication y el perfil docente local.")
    @ResponseStatus(HttpStatus.CREATED)
    public DocenteResponse crearDocente(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody CrearDocenteRequest r) { return service.crearDocente(actor, r); }
    @PutMapping("/docentes/{id}")
    @Operation(summary = "Actualizar docente", description = "Modifica los datos del docente y sincroniza su estado con Firebase.")
    public DocenteResponse actualizarDocente(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody ActualizarDocenteRequest r) {
        return service.actualizarDocente(actor, id, r);
    }
    @DeleteMapping("/docentes/{id}")
    @Operation(summary = "Inactivar docente", description = "Inactiva el perfil y deshabilita su acceso en Firebase.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarDocente(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        service.eliminarDocente(actor, id);
    }

    @GetMapping("/estudiantes/{id}/sociodemografico")
    @Operation(summary = "Consultar perfil sociodemográfico", description = "Obtiene los datos sociodemográficos del estudiante indicado.")
    public PerfilAdminResponse perfil(@PathVariable Long id) { return service.perfil(id); }
    @PutMapping("/estudiantes/{id}/sociodemografico")
    @Operation(summary = "Actualizar perfil sociodemográfico", description = "Crea o actualiza el perfil sociodemográfico del estudiante.")
    public PerfilAdminResponse guardarPerfil(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody PerfilRequest r) {
        return service.guardarPerfil(actor, id, r);
    }
}
