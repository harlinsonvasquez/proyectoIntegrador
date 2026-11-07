package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;

public interface RolJpaRepository extends JpaRepository<RolEntity, Long> {
    Optional<RolEntity> findByNombre(RolNombre nombre);
}
