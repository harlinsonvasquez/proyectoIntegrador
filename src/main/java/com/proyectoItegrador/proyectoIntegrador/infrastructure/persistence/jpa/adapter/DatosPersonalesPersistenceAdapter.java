package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.adapter;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.DatosPersonalesPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.UsuarioEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.AuditoriaCambioJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.UsuarioJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DatosPersonalesPersistenceAdapter implements DatosPersonalesPort {
    private final UsuarioJpaRepository usuarios;
    private final AuditoriaCambioJpaRepository auditorias;
    public DatosPersonalesPersistenceAdapter(UsuarioJpaRepository usuarios, AuditoriaCambioJpaRepository auditorias) {
        this.usuarios = usuarios;
        this.auditorias = auditorias;
    }
    public Optional<UsuarioEntity> usuario(Long id) { return usuarios.findById(id); }
    public UsuarioEntity guardar(UsuarioEntity user) { return usuarios.save(user); }
    public AuditoriaCambioEntity guardarAuditoria(AuditoriaCambioEntity audit) { return auditorias.save(audit); }
}
