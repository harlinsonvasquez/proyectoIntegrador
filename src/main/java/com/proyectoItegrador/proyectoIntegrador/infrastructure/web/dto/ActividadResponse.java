package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.CategoriaActividad;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ActividadResponse
         (Long id,
          CategoriaActividad categoria,
          String nombre,
        String descripcion,
          String organizacion,
          LocalDate fechaInicio,

          LocalDate fechaFin,
        String evidenciaUrl,
          OffsetDateTime creadaEn) {
}
