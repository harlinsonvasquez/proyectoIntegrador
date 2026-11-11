package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.adapter;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.ConsultaAcademicaPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.*;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ConsultaAcademicaPersistenceAdapter implements ConsultaAcademicaPort {
    private final EstudianteJpaRepository estudiantes;
    private final MatriculaAsignaturaJpaRepository matriculas;
    private final PlanEstudioJpaRepository planes;
    private final PlanAsignaturaJpaRepository asignaturas;
    private final PrerrequisitoJpaRepository prerrequisitos;
    private final PeriodoAcademicoJpaRepository periodos;
    private final OfertaAsignaturaJpaRepository ofertas;

    public ConsultaAcademicaPersistenceAdapter(EstudianteJpaRepository estudiantes,
            MatriculaAsignaturaJpaRepository matriculas, PlanEstudioJpaRepository planes,
            PlanAsignaturaJpaRepository asignaturas, PrerrequisitoJpaRepository prerrequisitos,
            PeriodoAcademicoJpaRepository periodos, OfertaAsignaturaJpaRepository ofertas) {
        this.estudiantes = estudiantes;
        this.matriculas = matriculas;
        this.planes = planes;
        this.asignaturas = asignaturas;
        this.prerrequisitos = prerrequisitos;
        this.periodos = periodos;
        this.ofertas = ofertas;
    }
    public Optional<EstudianteEntity> estudiante(Long id) { return estudiantes.findById(id); }
    public Optional<EstudianteEntity> estudiantePorUsuario(Long id) { return estudiantes.findByUsuario_Id(id); }
    public List<MatriculaAsignaturaEntity> matriculas(Long id) { return matriculas.findAllByEstudiante_Id(id); }
    public List<PlanEstudioEntity> planesActivos(Long id) { return planes.findAllByPrograma_IdAndActivoTrue(id); }
    public List<PlanAsignaturaEntity> asignaturasDelPlan(Long id) { return asignaturas.findAllByPlanEstudio_Id(id); }
    public List<PrerrequisitoEntity> prerrequisitos(Long id) { return prerrequisitos.findAllByPlanAsignatura_Id(id); }
    public Optional<PeriodoAcademicoEntity> periodo(Long id) { return periodos.findById(id); }
    public List<PeriodoAcademicoEntity> periodos() {
        return periodos.findAll().stream()
                .sorted(java.util.Comparator.comparing(PeriodoAcademicoEntity::getFechaInicio).reversed())
                .toList();
    }
    public List<OfertaAsignaturaEntity> ofertasDelProgramaEnPeriodo(Long programaId, Long periodoId) {
        return ofertas.findAllByPeriodo_IdAndPlanAsignatura_PlanEstudio_Programa_IdAndPlanAsignatura_PlanEstudio_ActivoTrue(
                periodoId, programaId);
    }
    public long matriculasActivasEnOferta(Long ofertaId) {
        return matriculas.countByOfertaAsignatura_IdAndEstadoIn(ofertaId,
                List.of(EstadoMatricula.PENDIENTE, EstadoMatricula.MATRICULADA, EstadoMatricula.CURSANDO));
    }
}
