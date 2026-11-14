package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import java.math.BigDecimal;
import java.util.List;

public record AsignaturaTrayectoriaResponse(Long planAsignaturaId, String codigo, String nombre,
        Short creditos, Short semestreSugerido, boolean obligatoria, String estado,
        EstadoMatricula estadoMatricula, BigDecimal nota, boolean prerrequisitosCumplidos,
        List<String> prerrequisitosPendientes) {
}
