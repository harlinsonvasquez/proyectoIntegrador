package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;

public interface AuditoriaCambioJpaRepository extends JpaRepository<AuditoriaCambioEntity, Long> {
}
