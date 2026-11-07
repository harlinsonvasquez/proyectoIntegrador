package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.ConsultaAcademicaPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AsignaturaTrayectoriaResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.PerfilEstudianteResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private List<AsignaturaTrayectoriaResponse> trayectoria(EstudianteEntity student) {
        List<MatriculaAsignaturaEntity> matriculas = persistence.matriculas(student.getId());
        Map<Long, MatriculaAsignaturaEntity> porPlanAsignatura = new HashMap<>();
        Set<Long> aprobadas = new HashSet<>();
        for (MatriculaAsignaturaEntity matricula : matriculas) {
            Long planAsignaturaId = matricula.getOfertaAsignatura().getPlanAsignatura().getId();
            porPlanAsignatura.put(planAsignaturaId, matricula);
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
