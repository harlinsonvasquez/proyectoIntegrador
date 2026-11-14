package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.ConsultaAcademicaPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AsignaturaTrayectoriaResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.IntentoAcademicoResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.OfertaDisponibleResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.PeriodoAcademicoResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.PerfilEstudianteResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultaAcademicaService {
    private final ConsultaAcademicaPort persistence;

    @Transactional(readOnly = true)
    public PerfilEstudianteResponse miPerfil(AuthenticatedUser actor) {
        exigir(actor, RolNombre.ESTUDIANTE);
        EstudianteEntity student = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> notFound("Perfil de estudiante no encontrado."));
        return perfil(student);
    }

    @Transactional(readOnly = true)
    public PerfilEstudianteResponse perfilParaDocente(AuthenticatedUser actor, Long id) {
        exigir(actor, RolNombre.DOCENTE, RolNombre.ADMINISTRADOR);
        return perfil(persistence.estudiante(id).orElseThrow(() -> notFound("Estudiante no encontrado.")));
    }

    @Transactional(readOnly = true)
    public List<AsignaturaTrayectoriaResponse> miTrayectoria(AuthenticatedUser actor) {
        exigir(actor, RolNombre.ESTUDIANTE);
        EstudianteEntity student = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> notFound("Perfil de estudiante no encontrado."));
        return trayectoria(student);
    }

    @Transactional(readOnly = true)
    public List<AsignaturaTrayectoriaResponse> trayectoriaParaDocente(AuthenticatedUser actor, Long id) {
        exigir(actor, RolNombre.DOCENTE, RolNombre.ADMINISTRADOR);
        EstudianteEntity student = persistence.estudiante(id)
                .orElseThrow(() -> notFound("Estudiante no encontrado."));
        return trayectoria(student);
    }

    @Transactional(readOnly = true)
    public List<IntentoAcademicoResponse> miHistorial(AuthenticatedUser actor) {
        exigir(actor, RolNombre.ESTUDIANTE);
        EstudianteEntity student = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> notFound("Perfil de estudiante no encontrado."));
        return historial(student);
    }

    @Transactional(readOnly = true)
    public List<IntentoAcademicoResponse> historialParaDocente(AuthenticatedUser actor, Long id) {
        exigir(actor, RolNombre.DOCENTE, RolNombre.ADMINISTRADOR);
        EstudianteEntity student = persistence.estudiante(id)
                .orElseThrow(() -> notFound("Estudiante no encontrado."));
        return historial(student);
    }

    @Transactional(readOnly = true)
    public List<PeriodoAcademicoResponse> periodos(AuthenticatedUser actor) {
        exigir(actor, RolNombre.ESTUDIANTE);
        return persistence.periodos().stream()
                .map(p -> new PeriodoAcademicoResponse(p.getId(), p.getCodigo(), p.getNombre(),
                        p.getFechaInicio(), p.getFechaFin()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfertaDisponibleResponse> ofertas(AuthenticatedUser actor, Long periodoId) {
        exigir(actor, RolNombre.ESTUDIANTE);
        EstudianteEntity student = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> notFound("Perfil de estudiante no encontrado."));
        var periodo = persistence.periodo(periodoId)
                .orElseThrow(() -> notFound("Periodo académico no encontrado."));

        List<MatriculaAsignaturaEntity> intentos = persistence.matriculas(student.getId());
        Set<Long> aprobadas = new HashSet<>();
        Set<Long> ofertasYaRegistradas = new HashSet<>();
        Set<Long> asignaturasEnCursoPeriodo = new HashSet<>();
        for (MatriculaAsignaturaEntity intento : intentos) {
            Long planAsignaturaId = intento.getOfertaAsignatura().getPlanAsignatura().getId();
            if (intento.getEstado() == EstadoMatricula.APROBADA) aprobadas.add(planAsignaturaId);
            ofertasYaRegistradas.add(intento.getOfertaAsignatura().getId());
            if (intento.getOfertaAsignatura().getPeriodo().getId().equals(periodoId)
                    && (intento.getEstado() == EstadoMatricula.MATRICULADA
                            || intento.getEstado() == EstadoMatricula.CURSANDO
                            || intento.getEstado() == EstadoMatricula.PENDIENTE)) {
                asignaturasEnCursoPeriodo.add(planAsignaturaId);
            }
        }

        return persistence.ofertasDelProgramaEnPeriodo(student.getPrograma().getId(), periodoId).stream()
                .filter(oferta -> oferta.getPlanAsignatura().getAsignatura().isActivo())
                .map(oferta -> {
                    var planAsignatura = oferta.getPlanAsignatura();
                    List<String> pendientes = persistence.prerrequisitos(planAsignatura.getId()).stream()
                            .filter(regla -> !aprobadas.contains(regla.getPrerrequisito().getId()))
                            .map(regla -> regla.getPrerrequisito().getAsignatura().getNombre())
                            .sorted().toList();
                    boolean requisitosCumplidos = pendientes.isEmpty();
                    long ocupados = persistence.matriculasActivasEnOferta(oferta.getId());
                    Integer cupo = oferta.getCupo();
                    boolean tieneCupo = cupo == null || ocupados < cupo;
                    boolean yaMatriculada = ofertasYaRegistradas.contains(oferta.getId())
                            || aprobadas.contains(planAsignatura.getId())
                            || asignaturasEnCursoPeriodo.contains(planAsignatura.getId());
                    Integer disponibles = cupo == null ? null : (int) Math.max(0, cupo - ocupados);
                    return new OfertaDisponibleResponse(oferta.getId(), planAsignatura.getId(),
                            planAsignatura.getAsignatura().getCodigo(), planAsignatura.getAsignatura().getNombre(),
                            planAsignatura.getAsignatura().getCreditos(), periodo.getCodigo(), periodo.getNombre(),
                            oferta.getGrupo(), oferta.getDocente() == null ? null : oferta.getDocente().getId(),
                            cupo, ocupados, disponibles, requisitosCumplidos, pendientes, yaMatriculada,
                            tieneCupo, requisitosCumplidos && tieneCupo && !yaMatriculada);
                })
                .sorted(Comparator.comparing(OfertaDisponibleResponse::codigoAsignatura)
                        .thenComparing(OfertaDisponibleResponse::grupo))
                .toList();
    }

    private List<IntentoAcademicoResponse> historial(EstudianteEntity student) {
        return persistence.matriculas(student.getId()).stream()
                .map(matricula -> {
                    var oferta = matricula.getOfertaAsignatura();
                    var planAsignatura = oferta.getPlanAsignatura();
                    var asignatura = planAsignatura.getAsignatura();
                    var periodo = oferta.getPeriodo();
                    return new IntentoAcademicoResponse(matricula.getId(), planAsignatura.getId(),
                            asignatura.getCodigo(), asignatura.getNombre(), periodo.getCodigo(),
                            periodo.getNombre(), oferta.getGrupo(), matricula.getEstado(),
                            matricula.getNotaFinal(), matricula.getFechaEstado());
                })
                .sorted(Comparator.comparing(IntentoAcademicoResponse::fechaEstado,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(IntentoAcademicoResponse::matriculaId,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<AsignaturaTrayectoriaResponse> trayectoria(EstudianteEntity student) {
        List<MatriculaAsignaturaEntity> matriculas = persistence.matriculas(student.getId());
        Map<Long, MatriculaAsignaturaEntity> porPlanAsignatura = new HashMap<>();
        Set<Long> aprobadas = new HashSet<>();
        for (MatriculaAsignaturaEntity matricula : matriculas) {
            Long planAsignaturaId = matricula.getOfertaAsignatura().getPlanAsignatura().getId();
            porPlanAsignatura.merge(planAsignaturaId, matricula, (actual, candidato) ->
                    actual.getFechaEstado() == null || (candidato.getFechaEstado() != null
                            && candidato.getFechaEstado().isAfter(actual.getFechaEstado()))
                            ? candidato : actual);
            if (matricula.getEstado() == EstadoMatricula.APROBADA) aprobadas.add(planAsignaturaId);
        }
        List<AsignaturaTrayectoriaResponse> result = new ArrayList<>();
        for (PlanEstudioEntity plan : persistence.planesActivos(student.getPrograma().getId())) {
            for (PlanAsignaturaEntity course : persistence.asignaturasDelPlan(plan.getId())) {
                List<String> pendientes = persistence.prerrequisitos(course.getId()).stream()
                        .filter(p -> !aprobadas.contains(p.getPrerrequisito().getId()))
                        .map(p -> p.getPrerrequisito().getAsignatura().getNombre()).toList();
                MatriculaAsignaturaEntity matricula = porPlanAsignatura.get(course.getId());
                String state = matricula == null ? (pendientes.isEmpty() ? "DISPONIBLE_POR_REQUISITOS" : "PENDIENTE_PRERREQUISITOS")
                        : matricula.getEstado().name();
                result.add(new AsignaturaTrayectoriaResponse(course.getId(), course.getAsignatura().getCodigo(),
                        course.getAsignatura().getNombre(), course.getAsignatura().getCreditos(),
                        course.getSemestreSugerido(), course.isObligatoria(), state,
                        matricula == null ? null : matricula.getEstado(),
                        matricula == null ? null : matricula.getNotaFinal(), pendientes.isEmpty(), pendientes));
            }
        }
        return result;
    }

    private PerfilEstudianteResponse perfil(EstudianteEntity student) {
        return new PerfilEstudianteResponse(student.getId(), student.getCodigoEstudiantil(),
                student.getUsuario().getNombre(), student.getUsuario().getApellido(),
                student.getUsuario().getCorreo(), student.getPrograma().getNombre(),
                student.getEstado(), student.getFechaIngreso());
    }

    private void exigir(AuthenticatedUser actor, RolNombre... roles) {
        for (RolNombre role : roles) if (actor.role() == role) return;
        throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Tu rol no permite consultar este recurso.");
    }
    private ReglaNegocioException notFound(String message) {
        return new ReglaNegocioException(HttpStatus.NOT_FOUND, message);
    }
}
