package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import java.time.OffsetDateTime;

public record PerfilSociodemograficoResponse(Long estudianteId, String municipioResidencia,
        Short estrato, String situacionLaboral, boolean requiereApoyo, String observacion,
        OffsetDateTime actualizadoEn) {
}
