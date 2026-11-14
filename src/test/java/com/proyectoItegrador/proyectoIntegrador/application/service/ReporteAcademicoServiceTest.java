package com.proyectoItegrador.proyectoIntegrador.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.ActividadEstudiantilJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.DocenteJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.MatriculaAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.OfertaAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.PeriodoAcademicoJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReporteAcademicoServiceTest {
    @Test
    void filtraDesempenoPorProgramaYPeriodo() {
        MatriculaAsignaturaJpaRepository enrollments = mock(MatriculaAsignaturaJpaRepository.class);
        ActividadEstudiantilJpaRepository activities = mock(ActividadEstudiantilJpaRepository.class);
        PeriodoAcademicoJpaRepository periods = mock(PeriodoAcademicoJpaRepository.class);
        DocenteJpaRepository teachers = mock(DocenteJpaRepository.class);
        OfertaAsignaturaJpaRepository offers = mock(OfertaAsignaturaJpaRepository.class);
        ReporteAcademicoService service = new ReporteAcademicoService(enrollments, activities, periods, teachers, offers);
        AuthenticatedUser actor = new AuthenticatedUser(8L, "docente@ejemplo.edu", RolNombre.DOCENTE);
        DocenteEntity teacher = mock(DocenteEntity.class);
        when(teacher.getId()).thenReturn(40L);
        when(teachers.findByUsuario_Id(8L)).thenReturn(java.util.Optional.of(teacher));
        when(periods.existsById(10L)).thenReturn(true);
        List<MatriculaAsignaturaEntity> rows = List.of(
                matricula(10L, EstadoMatricula.APROBADA, new BigDecimal("4.5")),
                matricula(11L, EstadoMatricula.REPROBADA, new BigDecimal("2.0")));
        rows.forEach(row -> {
            OfertaAsignaturaEntity offer = row.getOfertaAsignatura();
            when(offer.getDocente()).thenReturn(teacher);
        });
        when(enrollments.findAllByEstudiante_Programa_Id(5L)).thenReturn(rows);

        var report = service.desempeno(actor, 5L, 10L);

        assertEquals(1, report.totalMatriculas());
        assertEquals(1, report.aprobadas());
        assertEquals(0, report.reprobadas());
        assertEquals(new BigDecimal("4.5"), report.promedioNotas());
    }

    @Test
    void filtraParticipacionPorInterseccionDeFechasConElPeriodo() {
        MatriculaAsignaturaJpaRepository enrollments = mock(MatriculaAsignaturaJpaRepository.class);
        ActividadEstudiantilJpaRepository activities = mock(ActividadEstudiantilJpaRepository.class);
        PeriodoAcademicoJpaRepository periods = mock(PeriodoAcademicoJpaRepository.class);
        DocenteJpaRepository teachers = mock(DocenteJpaRepository.class);
        OfertaAsignaturaJpaRepository offers = mock(OfertaAsignaturaJpaRepository.class);
        ReporteAcademicoService service = new ReporteAcademicoService(enrollments, activities, periods, teachers, offers);
        AuthenticatedUser actor = new AuthenticatedUser(8L, "docente@ejemplo.edu", RolNombre.DOCENTE);
        DocenteEntity teacher = mock(DocenteEntity.class);
        when(teacher.getId()).thenReturn(40L);
        when(teachers.findByUsuario_Id(8L)).thenReturn(java.util.Optional.of(teacher));
        PeriodoAcademicoEntity period = mock(PeriodoAcademicoEntity.class);
        when(period.getFechaInicio()).thenReturn(LocalDate.parse("2026-01-01"));
        when(period.getFechaFin()).thenReturn(LocalDate.parse("2026-06-30"));
        when(periods.findById(10L)).thenReturn(java.util.Optional.of(period));
        List<ActividadEstudiantilEntity> rows = List.of(
                activity(CategoriaActividad.SEMILLERO, LocalDate.parse("2026-06-30"), LocalDate.parse("2026-07-02")),
                activity(CategoriaActividad.DEPORTE, LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-20")),
                activity(CategoriaActividad.OTRA, null, null));
        EstudianteEntity student = mock(EstudianteEntity.class);
        when(student.getId()).thenReturn(6L);
        rows.forEach(row -> when(row.getEstudiante()).thenReturn(student));
        MatriculaAsignaturaEntity enrollment = mock(MatriculaAsignaturaEntity.class);
        when(enrollment.getEstado()).thenReturn(EstadoMatricula.APROBADA);
        when(enrollment.getEstudiante()).thenReturn(student);
        when(enrollments.findAllByOfertaAsignatura_Docente_Id(40L)).thenReturn(List.of(enrollment));
        when(activities.findAll()).thenReturn(rows);

        var report = service.participacion(actor, null, 10L);

        assertEquals(1L, report.get(CategoriaActividad.SEMILLERO.name()));
        assertEquals(0L, report.get(CategoriaActividad.DEPORTE.name()));
        assertEquals(0L, report.get(CategoriaActividad.OTRA.name()));
    }

    private MatriculaAsignaturaEntity matricula(Long periodId, EstadoMatricula state, BigDecimal grade) {
        MatriculaAsignaturaEntity enrollment = mock(MatriculaAsignaturaEntity.class);
        OfertaAsignaturaEntity offer = mock(OfertaAsignaturaEntity.class);
        PeriodoAcademicoEntity period = mock(PeriodoAcademicoEntity.class);
        when(period.getId()).thenReturn(periodId);
        when(offer.getPeriodo()).thenReturn(period);
        DocenteEntity teacher = mock(DocenteEntity.class);
        when(teacher.getId()).thenReturn(40L);
        when(offer.getDocente()).thenReturn(teacher);
        when(enrollment.getOfertaAsignatura()).thenReturn(offer);
        when(enrollment.getEstado()).thenReturn(state);
        when(enrollment.getNotaFinal()).thenReturn(grade);
        return enrollment;
    }

    private ActividadEstudiantilEntity activity(CategoriaActividad category, LocalDate start, LocalDate end) {
        ActividadEstudiantilEntity activity = mock(ActividadEstudiantilEntity.class);
        when(activity.getCategoria()).thenReturn(category);
        when(activity.getFechaInicio()).thenReturn(start);
        when(activity.getFechaFin()).thenReturn(end);
        return activity;
    }
}
