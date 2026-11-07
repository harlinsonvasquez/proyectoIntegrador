package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.SolicitudApplicationService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoSolicitud;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActualizarSolicitudRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.CrearSolicitudRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.SolicitudResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Solicitudes de cancelación", description = "Creación, consulta y gestión del flujo de solicitudes y su estado.")
@SecurityRequirement(name = "firebaseBearer")
public class SolicitudController {
    private final SolicitudApplicationService service;

    @PostMapping("/solicitudes")
    @Operation(summary = "Crear solicitud de cancelación", description = "El estudiante solicita cancelar una matrícula propia y genera el registro inicial del flujo.")
    public SolicitudResponse crear(@AuthenticationPrincipal AuthenticatedUser actor,
            @Valid @RequestBody CrearSolicitudRequest request) {
        return service.crear(actor, request);
    }

    @GetMapping("/solicitudes/mias")
    @Operation(summary = "Listar mis solicitudes", description = "Devuelve las solicitudes del estudiante autenticado.")
    public List<SolicitudResponse> mias(@AuthenticationPrincipal AuthenticatedUser actor) {
        return service.misSolicitudes(actor);
    }

    @GetMapping("/solicitudes/{id}")
    @Operation(summary = "Consultar solicitud", description = "Obtiene el detalle de una solicitud si el usuario tiene acceso a ella.")
    public SolicitudResponse detalle(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id) {
        return service.detalle(actor, id);
    }

    @GetMapping("/docente/solicitudes")
    @Operation(summary = "Consultar bandeja docente", description = "Lista solicitudes accesibles al docente y permite filtrar por estado.")
    public List<SolicitudResponse> bandeja(@AuthenticationPrincipal AuthenticatedUser actor,
            @RequestParam(required = false) EstadoSolicitud estado) {
        return service.bandejaDocente(actor, estado);
    }

    @PatchMapping("/docente/solicitudes/{id}")
    @Operation(summary = "Procesar solicitud", description = "Avanza el estado de una solicitud y registra la observación en su seguimiento.")
    public SolicitudResponse actualizar(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable Long id,
            @Valid @RequestBody ActualizarSolicitudRequest request) {
        return service.actualizar(actor, id, request);
    }
}
