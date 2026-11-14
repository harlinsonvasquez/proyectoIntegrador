package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.PerfilSociodemograficoService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActualizarPerfilSociodemograficoRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.PerfilSociodemograficoResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Perfil sociodemográfico", description = "Consulta y actualización del perfil sociodemográfico de estudiantes.")
@SecurityRequirement(name = "firebaseBearer")
public class PerfilSociodemograficoController {
    private final PerfilSociodemograficoService service;

    @GetMapping("/me/sociodemografico")
    @Operation(summary = "Consultar mi perfil sociodemográfico", description = "Devuelve el perfil sociodemográfico del estudiante autenticado.")
    public PerfilSociodemograficoResponse mio(@AuthenticationPrincipal AuthenticatedUser actor) { return service.mio(actor); }
    @PutMapping("/me/sociodemografico")
    @Operation(summary = "Actualizar mi perfil sociodemográfico", description = "Crea o modifica los datos sociodemográficos propios.")
    public PerfilSociodemograficoResponse actualizar(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody ActualizarPerfilSociodemograficoRequest request) { return service.actualizar(actor, request); }
    @GetMapping("/{id}/sociodemografico")
    @Operation(summary = "Consultar perfil sociodemográfico ajeno", description = "Permite a un docente autorizado consultar el perfil del estudiante indicado.")
    public PerfilSociodemograficoResponse deEstudiante(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id) { return service.deEstudiante(actor, id); }
}
