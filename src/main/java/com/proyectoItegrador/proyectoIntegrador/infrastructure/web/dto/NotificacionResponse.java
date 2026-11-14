package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import java.time.OffsetDateTime;

public record NotificacionResponse(String id, String tipo, Long solicitudId,
        String estado, String mensaje, boolean leida, OffsetDateTime creadaEn) {
}
