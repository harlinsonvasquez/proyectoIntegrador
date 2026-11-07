package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.adapter;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.PerfilSociodemograficoPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.*;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PerfilSociodemograficoPersistenceAdapter implements PerfilSociodemograficoPort {
    private final EstudianteJpaRepository estudiantes;
    private final PerfilSociodemograficoJpaRepository perfiles;
    private final AuditoriaCambioJpaRepository auditorias;
    public PerfilSociodemograficoPersistenceAdapter(EstudianteJpaRepository estudiantes,
            PerfilSociodemograficoJpaRepository perfiles, AuditoriaCambioJpaRepository auditorias) {
        this.estudiantes = estudiantes;
        this.perfiles = perfiles;
        this.auditorias = auditorias;
    }
    public Optional<EstudianteEntity> estudiantePorUsuario(Long id) { return estudiantes.findByUsuario_Id(id); }
    public Optional<EstudianteEntity> estudiante(Long id) { return estudiantes.findById(id); }
    public Optional<PerfilSociodemograficoEntity> perfil(Long id) { return perfiles.findByEstudiante_Id(id); }
    public PerfilSociodemograficoEntity guardar(PerfilSociodemograficoEntity profile) { return perfiles.save(profile); }
    public AuditoriaCambioEntity guardarAuditoria(AuditoriaCambioEntity audit) { return auditorias.save(audit); }
}
