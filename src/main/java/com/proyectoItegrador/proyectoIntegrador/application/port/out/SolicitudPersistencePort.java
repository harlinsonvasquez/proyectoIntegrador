package com.proyectoItegrador.proyectoIntegrador.application.port.out;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import java.util.List;
import java.util.Optional;

public interface SolicitudPersistencePort {
    Optional<EstudianteEntity> estudiantePorUsuario(Long usuarioId);
    Optional<MatriculaAsignaturaEntity> matriculaDelEstudiante(Long matriculaId, Long estudianteId);
    Optional<SolicitudCancelacionEntity> solicitud(Long solicitudId);
    Optional<SolicitudCancelacionEntity> solicitudDelEstudiante(Long solicitudId, Long estudianteId);
    SolicitudCancelacionEntity guardar(SolicitudCancelacionEntity solicitud);
    SeguimientoSolicitudEntity guardarSeguimiento(SeguimientoSolicitudEntity seguimiento);
    AuditoriaCambioEntity guardarAuditoria(AuditoriaCambioEntity auditoria);
    OutboxEventoEntity guardarEvento(OutboxEventoEntity evento);
    List<SolicitudCancelacionEntity> solicitudesDelEstudiante(Long estudianteId);
    List<SolicitudCancelacionEntity> solicitudesPorEstado(EstadoSolicitud estado);
    List<SolicitudCancelacionEntity> todasLasSolicitudes();
    Optional<DocenteEntity> docentePorUsuario(Long usuarioId);
    Optional<UsuarioEntity> usuario(Long usuarioId);
    List<SeguimientoSolicitudEntity> seguimientos(Long solicitudId);
}
