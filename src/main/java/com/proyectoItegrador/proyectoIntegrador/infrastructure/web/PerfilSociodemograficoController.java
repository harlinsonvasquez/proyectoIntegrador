package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.PerfilSociodemograficoService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActualizarPerfilSociodemograficoRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.PerfilSociodemograficoResponse;
import jakarta.validation.Valid;
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
public class PerfilSociodemograficoController {
    private final PerfilSociodemograficoService service;

    @GetMapping("/me/sociodemografico")
    public PerfilSociodemograficoResponse mio(@AuthenticationPrincipal AuthenticatedUser actor) { return service.mio(actor); }
    @PutMapping("/me/sociodemografico")
    public PerfilSociodemograficoResponse actualizar(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody ActualizarPerfilSociodemograficoRequest request) { return service.actualizar(actor, request); }
    @GetMapping("/{id}/sociodemografico")
    public PerfilSociodemograficoResponse deEstudiante(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id) { return service.deEstudiante(actor, id); }
}
