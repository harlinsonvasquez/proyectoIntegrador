package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.adapter;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.ActividadPersistencePort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.ActividadEstudiantilEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstudianteEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.ActividadEstudiantilJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.EstudianteJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.AuditoriaCambioJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ActividadPersistenceAdapter implements ActividadPersistencePort {
    private final EstudianteJpaRepository estudiantes;
    private final ActividadEstudiantilJpaRepository actividades;
    private final AuditoriaCambioJpaRepository auditorias;
    public ActividadPersistenceAdapter(EstudianteJpaRepository estudiantes, ActividadEstudiantilJpaRepository actividades,
            AuditoriaCambioJpaRepository auditorias) {
        this.estudiantes = estudiantes;
        this.actividades = actividades;
        this.auditorias = auditorias;
    }
    public Optional<EstudianteEntity> estudiantePorUsuario(Long id) { return estudiantes.findByUsuario_Id(id); }
    public Optional<EstudianteEntity> estudiante(Long id) { return estudiantes.findById(id); }
    public List<ActividadEstudiantilEntity> actividades(Long id) {
        return actividades.findAllByEstudiante_IdOrderByFechaInicioDesc(id);
    }
    public ActividadEstudiantilEntity guardar(ActividadEstudiantilEntity activity) {
        return actividades.save(activity);
    }
    public AuditoriaCambioEntity guardarAuditoria(AuditoriaCambioEntity audit) { return auditorias.save(audit); }
}
