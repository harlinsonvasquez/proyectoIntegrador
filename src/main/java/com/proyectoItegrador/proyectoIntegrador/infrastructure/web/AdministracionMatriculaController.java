package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.AdministracionCrudService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.ActualizarMatriculaRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.MatriculaRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.MatriculaResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/matriculas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
@Tag(name = "Administración - Matrículas", description = "Consulta y mantiene la matrícula de asignaturas; la cancelación se gestiona por solicitudes.")
@SecurityRequirement(name = "firebaseBearer")
public class AdministracionMatriculaController {
    private final AdministracionCrudService service;

    @GetMapping
    @Operation(summary = "Listar matrículas", description = "Devuelve todas las matrículas de asignaturas.")
    public List<MatriculaResponse> matriculas() { return service.matriculas(); }
    @GetMapping("/{id}")
    @Operation(summary = "Consultar matrícula", description = "Obtiene una matrícula por su ID.")
    public MatriculaResponse matricula(@PathVariable Long id) { return service.matricula(id); }
    @PostMapping
    @Operation(summary = "Crear matrícula", description = "Registra una matrícula para un estudiante y una oferta; no acepta estado CANCELADA.")
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse crear(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody MatriculaRequest r) { return service.crearMatricula(actor, r); }
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar matrícula", description = "Actualiza estado y nota final; una matrícula cancelada no se reactiva desde esta ruta.")
    public MatriculaResponse actualizar(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id, @Valid @RequestBody ActualizarMatriculaRequest r) {
        return service.actualizarMatricula(actor, id, r);
    }
}
