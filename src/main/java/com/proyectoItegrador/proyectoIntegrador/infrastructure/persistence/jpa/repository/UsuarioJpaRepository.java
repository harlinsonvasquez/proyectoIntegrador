package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.UsuarioEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {
    @EntityGraph(attributePaths = "rol")
    Optional<UsuarioEntity> findByCorreoIgnoreCase(String correo);
    long countByRol_NombreAndActivoTrue(RolNombre nombre);
}
