package com.proyectoItegrador.proyectoIntegrador.application.port.out;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.UsuarioEntity;
import java.util.Optional;

public interface DatosPersonalesPort {
    Optional<UsuarioEntity> usuario(Long id);
    UsuarioEntity guardar(UsuarioEntity usuario);
    AuditoriaCambioEntity guardarAuditoria(AuditoriaCambioEntity auditoria);
}
