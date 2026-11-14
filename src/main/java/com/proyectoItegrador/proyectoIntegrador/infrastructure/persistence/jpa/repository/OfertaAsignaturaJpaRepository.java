package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OfertaAsignaturaEntity;

public interface OfertaAsignaturaJpaRepository extends JpaRepository<OfertaAsignaturaEntity, Long> {
    List<OfertaAsignaturaEntity> findAllByDocente_Id(Long docenteId);
    Optional<OfertaAsignaturaEntity> findByIdAndDocente_Id(Long id, Long docenteId);
    List<OfertaAsignaturaEntity> findAllByPeriodo_Id(Long periodoId);
    List<OfertaAsignaturaEntity> findAllByPeriodo_IdAndPlanAsignatura_PlanEstudio_Programa_IdAndPlanAsignatura_PlanEstudio_ActivoTrue(
            Long periodoId, Long programaId);
}
