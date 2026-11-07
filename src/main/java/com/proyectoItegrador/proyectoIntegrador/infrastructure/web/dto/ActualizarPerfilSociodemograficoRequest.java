package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilSociodemograficoRequest(@Size(max = 100) String municipioResidencia,
        @Min(1) @Max(6) Short estrato, @Size(max = 50) String situacionLaboral,
        boolean requiereApoyo, @Size(max = 1000) String observacion) {
}
