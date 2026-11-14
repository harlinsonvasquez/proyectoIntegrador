package com.proyectoItegrador.proyectoIntegrador.infrastructure.config;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;

public record AuthenticatedUser(Long userId, String email, RolNombre role) {
}
