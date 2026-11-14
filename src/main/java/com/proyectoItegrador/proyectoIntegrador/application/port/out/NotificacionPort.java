package com.proyectoItegrador.proyectoIntegrador.application.port.out;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotificacionResponse;
import java.util.List;

public interface NotificacionPort {
    List<NotificacionResponse> listar(Long userId);
    void marcarLeida(Long userId, String notificationId);
}
