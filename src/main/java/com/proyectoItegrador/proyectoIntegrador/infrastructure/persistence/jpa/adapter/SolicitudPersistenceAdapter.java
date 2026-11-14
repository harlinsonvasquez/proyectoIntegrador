package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.adapter;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.SolicitudPersistencePort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.*;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SolicitudPersistenceAdapter implements SolicitudPersistencePort {
    private final EstudianteJpaRepository estudiantes;
    private final MatriculaAsignaturaJpaRepository matriculas;
    private final SolicitudCancelacionJpaRepository solicitudes;
    private final SeguimientoSolicitudJpaRepository seguimientos;
    private final AuditoriaCambioJpaRepository auditorias;
    private final OutboxEventoJpaRepository eventos;
    private final DocenteJpaRepository docentes;
    private final UsuarioJpaRepository usuarios;

    public SolicitudPersistenceAdapter(EstudianteJpaRepository estudiantes,
            MatriculaAsignaturaJpaRepository matriculas, SolicitudCancelacionJpaRepository solicitudes,
            SeguimientoSolicitudJpaRepository seguimientos, AuditoriaCambioJpaRepository auditorias,
            OutboxEventoJpaRepository eventos, DocenteJpaRepository docentes, UsuarioJpaRepository usuarios) {
        this.estudiantes = estudiantes;
        this.matriculas = matriculas;
        this.solicitudes = solicitudes;
        this.seguimientos = seguimientos;
        this.auditorias = auditorias;
        this.eventos = eventos;
        this.docentes = docentes;
        this.usuarios = usuarios;
    }

    public Optional<EstudianteEntity> estudiantePorUsuario(Long id) { return estudiantes.findByUsuario_Id(id); }
    public Optional<MatriculaAsignaturaEntity> matriculaDelEstudiante(Long id, Long estudianteId) {
        return matriculas.findByIdAndEstudiante_Id(id, estudianteId);
    }
    public Optional<SolicitudCancelacionEntity> solicitud(Long id) { return solicitudes.findById(id); }
    public Optional<SolicitudCancelacionEntity> solicitudDelEstudiante(Long id, Long estudianteId) {
        return solicitudes.findByIdAndEstudiante_Id(id, estudianteId);
    }
    public SolicitudCancelacionEntity guardar(SolicitudCancelacionEntity entity) { return solicitudes.save(entity); }
    public SeguimientoSolicitudEntity guardarSeguimiento(SeguimientoSolicitudEntity entity) { return seguimientos.save(entity); }
    public AuditoriaCambioEntity guardarAuditoria(AuditoriaCambioEntity entity) { return auditorias.save(entity); }
    public OutboxEventoEntity guardarEvento(OutboxEventoEntity entity) { return eventos.save(entity); }
    public List<SolicitudCancelacionEntity> solicitudesDelEstudiante(Long id) {
        return solicitudes.findAllByEstudiante_IdOrderByFechaCreacionDesc(id);
    }
    public List<SolicitudCancelacionEntity> solicitudesPorEstado(EstadoSolicitud estado) {
        return solicitudes.findAllByEstadoOrderByFechaCreacionAsc(estado);
    }
    public List<SolicitudCancelacionEntity> todasLasSolicitudes() { return solicitudes.findAll(); }
    public Optional<DocenteEntity> docentePorUsuario(Long id) { return docentes.findByUsuario_Id(id); }
    public Optional<UsuarioEntity> usuario(Long id) { return usuarios.findById(id); }
    public List<SeguimientoSolicitudEntity> seguimientos(Long id) {
        return seguimientos.findAllBySolicitud_IdOrderByFechaAsc(id);
    }
}
