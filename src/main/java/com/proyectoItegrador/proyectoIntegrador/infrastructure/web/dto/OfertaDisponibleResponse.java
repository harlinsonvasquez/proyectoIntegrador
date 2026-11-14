package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import java.util.List;

/** Oferta existente y elegibilidad calculada desde el plan, prerrequisitos y matrículas. */
public record OfertaDisponibleResponse(
        Long ofertaId,
        Long planAsignaturaId,
        String codigoAsignatura,
        String asignatura,
        Short creditos,
        String codigoPeriodo,
        String periodo,
        String grupo,
        Long docenteId,
        Integer cupo,
        long cuposOcupados,
        Integer cuposDisponibles,
        boolean prerrequisitosCumplidos,
        List<String> prerrequisitosPendientes,
        boolean yaMatriculada,
        boolean cupoDisponible,
        boolean disponible) {
}
