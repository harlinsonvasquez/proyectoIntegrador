package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoSolicitud;
import java.time.OffsetDateTime;

public record SeguimientoResponse
(Long id, 
EstadoSolicitud estado,
 String observacion,
 String responsable,
  OffsetDateTime fecha) {
}
