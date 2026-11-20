package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EvaluacionAsignaturaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionAsignaturaJpaRepository extends JpaRepository<EvaluacionAsignaturaEntity, Long> {
    List<EvaluacionAsignaturaEntity> findAllByOferta_IdOrderByCreadaEnAsc(Long ofertaId);
    List<EvaluacionAsignaturaEntity> findAllByOferta_IdAndActivaTrueOrderByCreadaEnAsc(Long ofertaId);
    Optional<EvaluacionAsignaturaEntity> findByIdAndOferta_Id(Long id, Long ofertaId);
    List<EvaluacionAsignaturaEntity> findAllByOferta_IdInOrderByCreadaEnAsc(List<Long> ofertaIds);
    boolean existsByOferta_IdAndActivaTrue(Long ofertaId);
}
