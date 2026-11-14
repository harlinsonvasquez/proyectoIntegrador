package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.PrerrequisitoEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.PrerrequisitoId;

public interface PrerrequisitoJpaRepository extends JpaRepository<PrerrequisitoEntity, PrerrequisitoId> {
    List<PrerrequisitoEntity> findAllByPlanAsignatura_Id(Long planAsignaturaId);
}
