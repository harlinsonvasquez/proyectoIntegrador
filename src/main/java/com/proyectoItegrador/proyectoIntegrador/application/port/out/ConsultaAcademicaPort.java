package com.proyectoItegrador.proyectoIntegrador.application.port.out;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import java.util.List;
import java.util.Optional;

public interface ConsultaAcademicaPort {
    Optional<EstudianteEntity> estudiante(Long id);
    Optional<EstudianteEntity> estudiantePorUsuario(Long userId);
    List<MatriculaAsignaturaEntity> matriculas(Long estudianteId);
    List<PlanEstudioEntity> planesActivos(Long programaId);
    List<PlanAsignaturaEntity> asignaturasDelPlan(Long planEstudioId);
    List<PrerrequisitoEntity> prerrequisitos(Long planAsignaturaId);
    Optional<PeriodoAcademicoEntity> periodo(Long periodoId);
    List<PeriodoAcademicoEntity> periodos();
    List<OfertaAsignaturaEntity> ofertasDelProgramaEnPeriodo(Long programaId, Long periodoId);
    long matriculasActivasEnOferta(Long ofertaId);
}
