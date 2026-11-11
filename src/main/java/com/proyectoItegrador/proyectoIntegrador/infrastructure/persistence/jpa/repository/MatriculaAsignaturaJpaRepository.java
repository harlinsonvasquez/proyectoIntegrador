package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.MatriculaAsignaturaEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;

public interface MatriculaAsignaturaJpaRepository extends JpaRepository<MatriculaAsignaturaEntity, Long> {
    Optional<MatriculaAsignaturaEntity> findByIdAndEstudiante_Id(Long id, Long estudianteId);
    List<MatriculaAsignaturaEntity> findAllByEstudiante_Id(Long estudianteId);
    List<MatriculaAsignaturaEntity> findAllByEstudiante_Programa_Id(Long programaId);
    long countByOfertaAsignatura_IdAndEstadoIn(Long ofertaId, Collection<EstadoMatricula> estados);
}
