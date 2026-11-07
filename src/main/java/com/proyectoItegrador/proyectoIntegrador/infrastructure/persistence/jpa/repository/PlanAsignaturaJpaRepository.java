package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.PlanAsignaturaEntity;

public interface PlanAsignaturaJpaRepository extends JpaRepository<PlanAsignaturaEntity, Long> {
    List<PlanAsignaturaEntity> findAllByPlanEstudio_Id(Long planEstudioId);
}
