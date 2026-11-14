package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public record InscripcionEstudianteRequest(@NotNull Long ofertaId, @NotNull Long periodoId) {}
