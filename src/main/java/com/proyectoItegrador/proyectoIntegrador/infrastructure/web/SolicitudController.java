package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.SolicitudApplicationService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoSolicitud;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActualizarSolicitudRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.CrearSolicitudRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.SolicitudResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SolicitudController {
    private final SolicitudApplicationService service;

    @PostMapping("/solicitudes")
    public SolicitudResponse crear(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody CrearSolicitudRequest request) {
        return service.crear(actor, request);
    }

    @GetMapping("/solicitudes/mias")
    public List<SolicitudResponse> mias(@AuthenticationPrincipal AuthenticatedUser actor) {
        return service.misSolicitudes(actor);
    }

    @GetMapping("/solicitudes/{id}")
    public SolicitudResponse detalle(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        return service.detalle(actor, id);
    }

    @GetMapping("/docente/solicitudes")
    public List<SolicitudResponse> bandeja(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) EstadoSolicitud estado) {
        return service.bandejaDocente(actor, estado);
    }

    @PatchMapping("/docente/solicitudes/{id}")
    public SolicitudResponse actualizar(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id,
            @Valid @RequestBody ActualizarSolicitudRequest request) {
        return service.actualizar(actor, id, request);
    }
}
