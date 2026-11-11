package com.proyectoItegrador.proyectoIntegrador.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.ConsultaAcademicaPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ConsultaAcademicaServiceTest {
    @Test
    void historialConservaLosIntentosRepetidosYLosOrdenaPorFecha() {
        ConsultaAcademicaPort persistence = mock(ConsultaAcademicaPort.class);
        ConsultaAcademicaService service = new ConsultaAcademicaService(persistence);
        AuthenticatedUser actor = new AuthenticatedUser(4L, "estudiante@ejemplo.edu", RolNombre.ESTUDIANTE);
        EstudianteEntity student = mock(EstudianteEntity.class);
        when(student.getId()).thenReturn(44L);
        when(persistence.estudiantePorUsuario(4L)).thenReturn(Optional.of(student));
        MatriculaAsignaturaEntity antiguo = intento(100L, "2025-1", OffsetDateTime.parse("2025-05-01T00:00:00Z"),
                EstadoMatricula.REPROBADA, new BigDecimal("2.5"));
        MatriculaAsignaturaEntity reciente = intento(101L, "2025-2", OffsetDateTime.parse("2025-11-01T00:00:00Z"),
                EstadoMatricula.APROBADA, new BigDecimal("4.0"));
        when(persistence.matriculas(44L)).thenReturn(List.of(antiguo, reciente));

        var result = service.miHistorial(actor);

        assertEquals(2, result.size());
        assertEquals(List.of(101L, 100L), result.stream().map(r -> r.matriculaId()).toList());
        assertEquals(List.of("2025-2", "2025-1"), result.stream().map(r -> r.codigoPeriodo()).toList());
    }

    @Test
    void ofertasExplicanPrerrequisitosYAgotanCupoSinCambiarMatriculas() {
        ConsultaAcademicaPort persistence = mock(ConsultaAcademicaPort.class);
        ConsultaAcademicaService service = new ConsultaAcademicaService(persistence);
        AuthenticatedUser actor = new AuthenticatedUser(4L, "estudiante@ejemplo.edu", RolNombre.ESTUDIANTE);
        EstudianteEntity student = mock(EstudianteEntity.class);
        ProgramaEntity program = mock(ProgramaEntity.class);
        PeriodoAcademicoEntity period = mock(PeriodoAcademicoEntity.class);
        when(student.getId()).thenReturn(44L);
        when(student.getPrograma()).thenReturn(program);
        when(program.getId()).thenReturn(7L);
        when(period.getId()).thenReturn(9L);
        when(period.getCodigo()).thenReturn("2026-2");
        when(period.getNombre()).thenReturn("2026-2");
        when(persistence.estudiantePorUsuario(4L)).thenReturn(Optional.of(student));
        when(persistence.periodo(9L)).thenReturn(Optional.of(period));
        when(persistence.matriculas(44L)).thenReturn(List.of());

        AsignaturaEntity course = mock(AsignaturaEntity.class);
        when(course.isActivo()).thenReturn(true);
        when(course.getCodigo()).thenReturn("MAT-201");
        when(course.getNombre()).thenReturn("Cálculo II");
        when(course.getCreditos()).thenReturn((short) 4);
        PlanAsignaturaEntity courseInPlan = mock(PlanAsignaturaEntity.class);
        when(courseInPlan.getId()).thenReturn(301L);
        when(courseInPlan.getAsignatura()).thenReturn(course);
        PlanAsignaturaEntity prerequisite = mock(PlanAsignaturaEntity.class);
        when(prerequisite.getId()).thenReturn(100L);
        AsignaturaEntity prerequisiteCourse = mock(AsignaturaEntity.class);
        when(prerequisiteCourse.getNombre()).thenReturn("Cálculo I");
        when(prerequisite.getAsignatura()).thenReturn(prerequisiteCourse);
        PrerrequisitoEntity rule = mock(PrerrequisitoEntity.class);
        when(rule.getPrerrequisito()).thenReturn(prerequisite);

        OfertaAsignaturaEntity offer = mock(OfertaAsignaturaEntity.class);
        when(offer.getId()).thenReturn(801L);
        when(offer.getPlanAsignatura()).thenReturn(courseInPlan);
        when(offer.getPeriodo()).thenReturn(period);
        when(offer.getGrupo()).thenReturn("A");
        when(offer.getCupo()).thenReturn(2);
        PlanAsignaturaEntity courseInPlan2 = mock(PlanAsignaturaEntity.class);
        when(courseInPlan2.getId()).thenReturn(302L);
        when(courseInPlan2.getAsignatura()).thenReturn(course);
        OfertaAsignaturaEntity offerWithRoom = mock(OfertaAsignaturaEntity.class);
        when(offerWithRoom.getId()).thenReturn(802L);
        when(offerWithRoom.getPlanAsignatura()).thenReturn(courseInPlan2);
        when(offerWithRoom.getPeriodo()).thenReturn(period);
        when(offerWithRoom.getGrupo()).thenReturn("B");
        when(offerWithRoom.getCupo()).thenReturn(4);
        when(persistence.ofertasDelProgramaEnPeriodo(7L, 9L)).thenReturn(List.of(offer, offerWithRoom));
        when(persistence.prerrequisitos(301L)).thenReturn(List.of(rule));
        when(persistence.prerrequisitos(302L)).thenReturn(List.of());
        when(persistence.matriculasActivasEnOferta(801L)).thenReturn(2L);
        when(persistence.matriculasActivasEnOferta(802L)).thenReturn(1L);

        var result = service.ofertas(actor, 9L);

        assertEquals(2, result.size());
        var offered = result.get(0);
        assertFalse(offered.prerrequisitosCumplidos());
        assertEquals(List.of("Cálculo I"), offered.prerrequisitosPendientes());
        assertEquals(0, offered.cuposDisponibles());
        assertFalse(offered.cupoDisponible());
        assertFalse(offered.disponible());
        var available = result.get(1);
        assertTrue(available.prerrequisitosCumplidos());
        assertEquals(3, available.cuposDisponibles());
        assertTrue(available.cupoDisponible());
        assertTrue(available.disponible());
    }

    private MatriculaAsignaturaEntity intento(Long id, String term, OffsetDateTime date,
            EstadoMatricula status, BigDecimal grade) {
        MatriculaAsignaturaEntity enrollment = mock(MatriculaAsignaturaEntity.class);
        OfertaAsignaturaEntity offer = mock(OfertaAsignaturaEntity.class);
        PlanAsignaturaEntity planCourse = mock(PlanAsignaturaEntity.class);
        AsignaturaEntity course = mock(AsignaturaEntity.class);
        PeriodoAcademicoEntity period = mock(PeriodoAcademicoEntity.class);
        when(enrollment.getId()).thenReturn(id);
        when(enrollment.getEstado()).thenReturn(status);
        when(enrollment.getNotaFinal()).thenReturn(grade);
        when(enrollment.getFechaEstado()).thenReturn(date);
        when(enrollment.getOfertaAsignatura()).thenReturn(offer);
        when(offer.getPlanAsignatura()).thenReturn(planCourse);
        when(offer.getPeriodo()).thenReturn(period);
        when(offer.getGrupo()).thenReturn("A");
        when(planCourse.getId()).thenReturn(17L);
        when(planCourse.getAsignatura()).thenReturn(course);
        when(course.getCodigo()).thenReturn("MAT-101");
        when(course.getNombre()).thenReturn("Cálculo I");
        when(period.getCodigo()).thenReturn(term);
        when(period.getNombre()).thenReturn(term);
        return enrollment;
    }
}
