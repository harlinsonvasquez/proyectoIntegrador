package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoItegrador.proyectoIntegrador.application.port.out.SolicitudPersistencePort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolicitudApplicationService {
    private final SolicitudPersistencePort persistence;
    private final ObjectMapper mapper;

    @Transactional
    public SolicitudResponse crear(AuthenticatedUser actor, CrearSolicitudRequest request) {
        exigirRol(actor, RolNombre.ESTUDIANTE);
        EstudianteEntity estudiante = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> noEncontrado("El usuario no tiene perfil de estudiante."));
        MatriculaAsignaturaEntity matricula = persistence.matriculaDelEstudiante(
                request.matriculaId(), estudiante.getId())
                .orElseThrow(() -> noEncontrado("No existe esa matrícula para el estudiante autenticado."));
        if (matricula.getEstado() != EstadoMatricula.MATRICULADA
                && matricula.getEstado() != EstadoMatricula.CURSANDO) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Solo se puede solicitar la cancelación de una matrícula activa.");
        }

        SolicitudCancelacionEntity solicitud = persistence.guardar(
                new SolicitudCancelacionEntity(estudiante, matricula, request.motivo().trim()));
        persistence.guardarSeguimiento(new SeguimientoSolicitudEntity(
                solicitud, estudiante.getUsuario(), EstadoSolicitud.PENDIENTE, "Solicitud creada."));
        guardarAuditoria(estudiante.getUsuario(), "solicitud_cancelacion", solicitud.getId(), null,
                Map.of("estado", EstadoSolicitud.PENDIENTE.name(), "motivo", solicitud.getMotivo()));
        guardarEvento(estudiante.getUsuario(), solicitud, "SOLICITUD_CREADA");
        return respuesta(solicitud, List.of(persistence.seguimientos(solicitud.getId()).get(0)));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> misSolicitudes(AuthenticatedUser actor) {
        exigirRol(actor, RolNombre.ESTUDIANTE);
        EstudianteEntity estudiante = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> noEncontrado("El usuario no tiene perfil de estudiante."));
        return persistence.solicitudesDelEstudiante(estudiante.getId()).stream()
                .map(s -> respuesta(s, persistence.seguimientos(s.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse detalle(AuthenticatedUser actor, Long id) {
        SolicitudCancelacionEntity solicitud;
        if (actor.role() == RolNombre.ESTUDIANTE) {
            EstudianteEntity estudiante = persistence.estudiantePorUsuario(actor.userId())
                    .orElseThrow(() -> noEncontrado("El usuario no tiene perfil de estudiante."));
            solicitud = persistence.solicitudDelEstudiante(id, estudiante.getId())
                    .orElseThrow(() -> noEncontrado("Solicitud no encontrada."));
        } else {
            exigirRol(actor, RolNombre.DOCENTE, RolNombre.ADMINISTRADOR);
            solicitud = persistence.solicitud(id).orElseThrow(() -> noEncontrado("Solicitud no encontrada."));
        }
        return respuesta(solicitud, persistence.seguimientos(id));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> bandejaDocente(AuthenticatedUser actor, EstadoSolicitud estado) {
        exigirRol(actor, RolNombre.DOCENTE, RolNombre.ADMINISTRADOR);
        List<SolicitudCancelacionEntity> solicitudes = estado == null
                ? persistence.solicitudesPorEstado(EstadoSolicitud.PENDIENTE)
                : persistence.solicitudesPorEstado(estado);
        return solicitudes.stream()
                .filter(s -> actor.role() == RolNombre.ADMINISTRADOR || s.getAsignadaDocente() == null
                        || s.getAsignadaDocente().getUsuario().getId().equals(actor.userId()))
                .map(s -> respuesta(s, persistence.seguimientos(s.getId()))).toList();
    }

    @Transactional
    public SolicitudResponse actualizar(AuthenticatedUser actor, Long id, ActualizarSolicitudRequest request) {
        exigirRol(actor, RolNombre.DOCENTE, RolNombre.ADMINISTRADOR);
        UsuarioEntity usuario = persistence.usuario(actor.userId())
                .orElseThrow(() -> noEncontrado("Usuario autenticado no encontrado."));
        DocenteEntity docente = actor.role() == RolNombre.DOCENTE
                ? persistence.docentePorUsuario(actor.userId())
                    .orElseThrow(() -> noEncontrado("El usuario no tiene perfil docente."))
                : null;
        SolicitudCancelacionEntity solicitud = persistence.solicitud(id)
                .orElseThrow(() -> noEncontrado("Solicitud no encontrada."));
        if (actor.role() == RolNombre.DOCENTE && solicitud.getAsignadaDocente() != null
                && !solicitud.getAsignadaDocente().getId().equals(docente.getId())) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "La solicitud está asignada a otro docente.");
        }
        EstadoSolicitud anterior = solicitud.getEstado();
        EstadoSolicitud nuevo = request.estado();
        boolean transicionValida = (anterior == EstadoSolicitud.PENDIENTE
                    && nuevo == EstadoSolicitud.EN_REVISION)
                || (anterior == EstadoSolicitud.EN_REVISION
                    && (nuevo == EstadoSolicitud.APROBADA || nuevo == EstadoSolicitud.RECHAZADA));
        if (!transicionValida) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Transición no permitida: " + anterior + " -> " + nuevo);
        }

        solicitud.actualizarEstado(nuevo, docente);
        if (nuevo == EstadoSolicitud.APROBADA) {
            MatriculaAsignaturaEntity matricula = solicitud.getMatricula();
            String anteriorMatricula = matricula.getEstado().name();
            matricula.cancelar();
            guardarAuditoria(usuario, "matricula_asignatura", matricula.getId(),
                    Map.of("estado", anteriorMatricula), Map.of("estado", "CANCELADA"));
        }
        persistence.guardar(solicitud);
        persistence.guardarSeguimiento(new SeguimientoSolicitudEntity(
                solicitud, usuario, nuevo, request.observacion()));
        guardarAuditoria(usuario, "solicitud_cancelacion", solicitud.getId(),
                Map.of("estado", anterior.name()), Map.of("estado", nuevo.name()));
        guardarEvento(solicitud.getEstudiante().getUsuario(), solicitud, "SOLICITUD_ACTUALIZADA");
        return respuesta(solicitud, persistence.seguimientos(id));
    }

    private void guardarEvento(UsuarioEntity destino, SolicitudCancelacionEntity solicitud, String tipo) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tipo", tipo);
        payload.put("solicitudId", solicitud.getId());
        payload.put("estado", solicitud.getEstado().name());
        payload.put("mensaje", "La solicitud " + solicitud.getId() + " está " + solicitud.getEstado());
        payload.put("codigoEstudiantil", solicitud.getEstudiante().getCodigoEstudiantil());
        persistence.guardarEvento(new OutboxEventoEntity(tipo, "solicitud_cancelacion",
                solicitud.getId().toString(), destino, mapper.valueToTree(payload)));
    }

    private void guardarAuditoria(UsuarioEntity actor, String entidad, Long id,
            Map<String, Object> anteriores, Map<String, Object> nuevos) {
        persistence.guardarAuditoria(new AuditoriaCambioEntity(actor, entidad, id.toString(),
                anteriores == null ? OperacionAuditoria.INSERT : OperacionAuditoria.UPDATE,
                anteriores == null ? null : mapper.valueToTree(anteriores),
                mapper.valueToTree(nuevos)));
    }

    private SolicitudResponse respuesta(SolicitudCancelacionEntity s, List<SeguimientoSolicitudEntity> seguimiento) {
        var matricula = s.getMatricula();
        String asignatura = matricula.getOfertaAsignatura().getPlanAsignatura().getAsignatura().getNombre();
        String periodo = matricula.getOfertaAsignatura().getPeriodo().getNombre();
        List<SeguimientoResponse> historial = seguimiento.stream().map(item -> new SeguimientoResponse(
                item.getId(), item.getEstado(), item.getObservacion(),
                item.getResponsable().getNombre() + " " + item.getResponsable().getApellido(), item.getFecha())).toList();
        return new SolicitudResponse(s.getId(), s.getEstudiante().getId(), matricula.getId(), asignatura,
                periodo, s.getEstado(), s.getMotivo(), s.getFechaCreacion(), s.getFechaCierre(), historial);
    }

    private void exigirRol(AuthenticatedUser actor, RolNombre... roles) {
        for (RolNombre role : roles) if (actor.role() == role) return;
        throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Tu rol no permite realizar esta operación.");
    }

    private ReglaNegocioException noEncontrado(String message) {
        return new ReglaNegocioException(HttpStatus.NOT_FOUND, message);
    }
}
