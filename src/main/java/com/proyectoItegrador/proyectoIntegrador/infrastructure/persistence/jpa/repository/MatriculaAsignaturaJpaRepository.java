package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.MatriculaAsignaturaEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;

public interface MatriculaAsignaturaJpaRepository extends JpaRepository<MatriculaAsignaturaEntity, Long> {
    Optional<MatriculaAsignaturaEntity> findByIdAndEstudiante_Id(Long id, Long estudianteId);
    Optional<MatriculaAsignaturaEntity> findByIdAndOfertaAsignatura_Docente_Id(Long id, Long docenteId);
    List<MatriculaAsignaturaEntity> findAllByOfertaAsignatura_Id(Long ofertaId);
    List<MatriculaAsignaturaEntity> findAllByEstudiante_Id(Long estudianteId);
    List<MatriculaAsignaturaEntity> findAllByEstudiante_Programa_Id(Long programaId);
    List<MatriculaAsignaturaEntity> findAllByOfertaAsignatura_Docente_IdAndEstadoIn(
            Long docenteId, Collection<EstadoMatricula> estados);
    List<MatriculaAsignaturaEntity> findAllByOfertaAsignatura_Docente_Id(Long docenteId);
    long countByOfertaAsignatura_IdAndEstadoIn(Long ofertaId, Collection<EstadoMatricula> estados);
}
