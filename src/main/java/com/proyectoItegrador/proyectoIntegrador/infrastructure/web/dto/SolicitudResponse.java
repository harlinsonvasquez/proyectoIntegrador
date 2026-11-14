package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoSolicitud;
import java.time.OffsetDateTime;
import java.util.List;

public record SolicitudResponse(Long id, Long estudianteId, Long matriculaId,
        String asignatura, String periodo, EstadoSolicitud estado, String motivo,
        OffsetDateTime fechaCreacion, OffsetDateTime fechaCierre,
        List<SeguimientoResponse> seguimientos) {
}
