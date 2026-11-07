package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.MatriculaAsignaturaEntity;

public interface MatriculaAsignaturaJpaRepository extends JpaRepository<MatriculaAsignaturaEntity, Long> {
    Optional<MatriculaAsignaturaEntity> findByIdAndEstudiante_Id(Long id, Long estudianteId);
    List<MatriculaAsignaturaEntity> findAllByEstudiante_Id(Long estudianteId);
    List<MatriculaAsignaturaEntity> findAllByEstudiante_Programa_Id(Long programaId);
}
