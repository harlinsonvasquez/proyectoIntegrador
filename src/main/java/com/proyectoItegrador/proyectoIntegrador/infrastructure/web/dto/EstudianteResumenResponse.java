package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoEstudiante;

public record EstudianteResumenResponse(Long id, String codigoEstudiantil,
        String nombreCompleto, String correo, String programa, EstadoEstudiante estado) {
}
