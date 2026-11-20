package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OfertaAsignaturaEntity;

public interface OfertaAsignaturaJpaRepository extends JpaRepository<OfertaAsignaturaEntity, Long> {
    List<OfertaAsignaturaEntity> findAllByDocente_Id(Long docenteId);
    Optional<OfertaAsignaturaEntity> findByIdAndDocente_Id(Long id, Long docenteId);
    List<OfertaAsignaturaEntity> findAllByPeriodo_Id(Long periodoId);
    List<OfertaAsignaturaEntity> findAllByPeriodo_IdAndPlanAsignatura_PlanEstudio_Programa_IdAndPlanAsignatura_PlanEstudio_ActivoTrue(
            Long periodoId, Long programaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OfertaAsignaturaEntity o where o.id = :id and o.docente.id = :docenteId")
    Optional<OfertaAsignaturaEntity> bloquearPorIdYDocente(@Param("id") Long id,
            @Param("docenteId") Long docenteId);
}
