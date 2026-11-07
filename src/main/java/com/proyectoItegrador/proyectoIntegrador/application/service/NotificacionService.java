package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.NotificacionPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotificacionResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.firebase.RealtimeDatabaseNotificacionAdapter.NotificacionNoEncontradaException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
@RequiredArgsConstructor
public class NotificacionService {
    private final NotificacionPort notifications;

    public List<NotificacionResponse> listar(AuthenticatedUser actor) { return notifications.listar(actor.userId()); }
    public void marcarLeida(AuthenticatedUser actor, String id) {
        try {
            notifications.marcarLeida(actor.userId(), id);
        } catch (NotificacionNoEncontradaException ex) {
            throw new ReglaNegocioException(HttpStatus.NOT_FOUND, "Notificación no encontrada.");
        }
    }
}
