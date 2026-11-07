package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.PlanEstudioEntity;

public interface PlanEstudioJpaRepository extends JpaRepository<PlanEstudioEntity, Long> {
    List<PlanEstudioEntity> findAllByPrograma_IdAndActivoTrue(Long programaId);
}
