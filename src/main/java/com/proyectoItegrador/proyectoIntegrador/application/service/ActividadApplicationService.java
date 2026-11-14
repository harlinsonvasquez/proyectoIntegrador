package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoItegrador.proyectoIntegrador.application.port.out.ActividadPersistencePort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.ActividadEstudiantilEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OperacionAuditoria;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActividadResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.CrearActividadRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActividadApplicationService {
    private final ActividadPersistencePort persistence;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public List<ActividadResponse> mias(AuthenticatedUser actor) {
        exigir(actor, RolNombre.ESTUDIANTE);
        var student = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> notFound("Perfil de estudiante no encontrado."));
        return listar(student.getId());
    }

    @Transactional(readOnly = true)
    public List<ActividadResponse> deEstudiante(AuthenticatedUser actor, Long id) {
        exigir(actor, RolNombre.DOCENTE, RolNombre.ADMINISTRADOR);
        persistence.estudiante(id).orElseThrow(() -> notFound("Estudiante no encontrado."));
        return listar(id);
    }

    @Transactional
    public ActividadResponse registrar(AuthenticatedUser actor, CrearActividadRequest request) {
        exigir(actor, RolNombre.ESTUDIANTE);
        if (request.fechaInicio() != null && request.fechaFin() != null
                && request.fechaFin().isBefore(request.fechaInicio())) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La fecha final no puede ser anterior a la inicial.");
        }
        var student = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> notFound("Perfil de estudiante no encontrado."));
        var activity = persistence.guardar(new ActividadEstudiantilEntity(student, request.categoria(),
                request.nombre().trim(), request.descripcion(), request.organizacion(), request.fechaInicio(),
                request.fechaFin(), request.evidenciaUrl(), student.getUsuario()));
        var values = Map.of("categoria", activity.getCategoria().name(), "nombre", activity.getNombre(),
                "fechaInicio", activity.getFechaInicio() == null ? "" : activity.getFechaInicio().toString(),
                "fechaFin", activity.getFechaFin() == null ? "" : activity.getFechaFin().toString());
        persistence.guardarAuditoria(new AuditoriaCambioEntity(student.getUsuario(), "actividad_estudiantil",
                activity.getId().toString(), OperacionAuditoria.INSERT, null, mapper.valueToTree(values)));
        return response(activity);
    }

    private List<ActividadResponse> listar(Long id) {
        return persistence.actividades(id).stream().map(this::response).toList();
    }
    private ActividadResponse response(ActividadEstudiantilEntity a) {
        return new ActividadResponse(a.getId(), a.getCategoria(), a.getNombre(), a.getDescripcion(),
                a.getOrganizacion(), a.getFechaInicio(), a.getFechaFin(), a.getEvidenciaUrl(), a.getCreadaEn());
    }
    private void exigir(AuthenticatedUser actor, RolNombre... roles) {
        for (RolNombre role : roles) if (actor.role() == role) return;
        throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Tu rol no permite realizar esta operación.");
    }
    private ReglaNegocioException notFound(String message) {
        return new ReglaNegocioException(HttpStatus.NOT_FOUND, message);
    }
}
