package com.proyectoItegrador.proyectoIntegrador.application.port.out;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.ActividadEstudiantilEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstudianteEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;
import java.util.List;
import java.util.Optional;

public interface ActividadPersistencePort {
    Optional<EstudianteEntity> estudiantePorUsuario(Long userId);
    Optional<EstudianteEntity> estudiante(Long id);
    List<ActividadEstudiantilEntity> actividades(Long studentId);
    ActividadEstudiantilEntity guardar(ActividadEstudiantilEntity activity);
    AuditoriaCambioEntity guardarAuditoria(AuditoriaCambioEntity audit);
}
