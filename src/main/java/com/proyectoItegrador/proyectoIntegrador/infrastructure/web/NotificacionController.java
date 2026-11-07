package com.proyectoItegrador.proyectoIntegrador.infrastructure.web;

import com.proyectoItegrador.proyectoIntegrador.application.service.NotificacionService;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotificacionResponse;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Consulta las notificaciones de Firebase Realtime Database.")
@SecurityRequirement(name = "firebaseBearer")
public class NotificacionController {
    private final NotificacionService service;

    @GetMapping
    @Operation(summary = "Listar mis notificaciones", description = "Devuelve las notificaciones del usuario autenticado; requiere Firebase habilitado.")
    public List<NotificacionResponse> listar(@AuthenticationPrincipal AuthenticatedUser actor) {
        return service.listar(actor);
    }
    @PatchMapping("/{id}/leida")
    @Operation(summary = "Marcar notificación como leída", description = "Marca como leída una notificación propia identificada por su ID.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarLeida(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable String id) {
        service.marcarLeida(actor, id);
    }
}
