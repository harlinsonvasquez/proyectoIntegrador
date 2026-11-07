package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.ConsultaAcademicaService;
import com.proyectoItegrador.proyectoIntegrador.application.service.DatosPersonalesService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AsignaturaTrayectoriaResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.PerfilEstudianteResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActualizarDatosPersonalesRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DatosPersonalesResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@Tag(name = "Consulta académica", description = "Consulta perfiles y trayectoria académica de estudiantes.")
@SecurityRequirement(name = "firebaseBearer")
public class ConsultaAcademicaController {
    private final ConsultaAcademicaService service;
    private final DatosPersonalesService datosPersonales;

    @GetMapping("/me")
    @Operation(summary = "Consultar mi perfil", description = "Devuelve el perfil del estudiante autenticado.")
    public PerfilEstudianteResponse miPerfil(@AuthenticationPrincipal AuthenticatedUser actor) {
        return service.miPerfil(actor);
    }

    @PutMapping("/me/datos-personales")
    @Operation(summary = "Actualizar mis datos personales", description = "Actualiza nombre y apellido del usuario estudiante autenticado.")
    public DatosPersonalesResponse actualizarDatos(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody ActualizarDatosPersonalesRequest request) {
        return datosPersonales.actualizar(actor, request);
    }

    @GetMapping("/me/trayectoria")
    @Operation(summary = "Consultar mi trayectoria", description = "Lista asignaturas del plan, estado académico y datos de prerrequisitos.")
    public List<AsignaturaTrayectoriaResponse> miTrayectoria(@AuthenticationPrincipal AuthenticatedUser actor) {
        return service.miTrayectoria(actor);
    }

    @GetMapping("/{id}/perfil")
    @Operation(summary = "Consultar perfil de estudiante", description = "Permite a un docente autorizado consultar el perfil del estudiante indicado.")
    public PerfilEstudianteResponse perfil(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        return service.perfilParaDocente(actor, id);
    }

    @GetMapping("/{id}/trayectoria")
    @Operation(summary = "Consultar trayectoria de estudiante", description = "Permite a un docente autorizado consultar la trayectoria del estudiante indicado.")
    public List<AsignaturaTrayectoriaResponse> trayectoria(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id) {
        return service.trayectoriaParaDocente(actor, id);
    }
}
