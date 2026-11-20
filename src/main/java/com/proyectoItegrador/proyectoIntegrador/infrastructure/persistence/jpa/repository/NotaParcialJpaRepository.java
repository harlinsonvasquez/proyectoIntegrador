package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.NotaParcialEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaParcialJpaRepository extends JpaRepository<NotaParcialEntity, Long> {
    List<NotaParcialEntity> findAllByEvaluacion_IdOrderByMatricula_Estudiante_Usuario_ApellidoAsc(
            Long evaluacionId);
    List<NotaParcialEntity> findAllByMatricula_Estudiante_IdOrderByIdDesc(Long estudianteId);
    Optional<NotaParcialEntity> findByEvaluacion_IdAndMatricula_Id(Long evaluacionId, Long matriculaId);
    List<NotaParcialEntity> findAllByEvaluacion_IdIn(List<Long> evaluacionIds);
}
