package com.proyectoItegrador.proyectoIntegrador.application.port.out;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import java.util.Optional;

public interface PerfilSociodemograficoPort {
    Optional<EstudianteEntity> estudiantePorUsuario(Long userId);
    Optional<EstudianteEntity> estudiante(Long id);
    Optional<PerfilSociodemograficoEntity> perfil(Long studentId);
    PerfilSociodemograficoEntity guardar(PerfilSociodemograficoEntity profile);
    AuditoriaCambioEntity guardarAuditoria(AuditoriaCambioEntity audit);
}
