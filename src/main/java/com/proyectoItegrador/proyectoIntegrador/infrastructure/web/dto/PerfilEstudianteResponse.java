package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoEstudiante;
import java.time.LocalDate;

public record PerfilEstudianteResponse(Long id, String codigoEstudiantil, String nombre,
        String apellido, String correo, String programa, EstadoEstudiante estado, LocalDate fechaIngreso) {
}
