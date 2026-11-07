package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.ConsultaAcademicaService;
import com.proyectoItegrador.proyectoIntegrador.application.service.DatosPersonalesService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AsignaturaTrayectoriaResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.PerfilEstudianteResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActualizarDatosPersonalesRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DatosPersonalesResponse;
import jakarta.validation.Valid;
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
public class ConsultaAcademicaController {
    private final ConsultaAcademicaService service;
    private final DatosPersonalesService datosPersonales;

    @GetMapping("/me")
    public PerfilEstudianteResponse miPerfil(@AuthenticationPrincipal AuthenticatedUser actor) {
        return service.miPerfil(actor);
    }

    @PutMapping("/me/datos-personales")
    public DatosPersonalesResponse actualizarDatos(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody ActualizarDatosPersonalesRequest request) {
        return datosPersonales.actualizar(actor, request);
    }

    @GetMapping("/me/trayectoria")
    public List<AsignaturaTrayectoriaResponse> miTrayectoria(@AuthenticationPrincipal AuthenticatedUser actor) {
        return service.miTrayectoria(actor);
    }

    @GetMapping("/{id}/perfil")
    public PerfilEstudianteResponse perfil(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        return service.perfilParaDocente(actor, id);
    }

    @GetMapping("/{id}/trayectoria")
    public List<AsignaturaTrayectoriaResponse> trayectoria(@AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable Long id) {
        return service.trayectoriaParaDocente(actor, id);
    }
}
