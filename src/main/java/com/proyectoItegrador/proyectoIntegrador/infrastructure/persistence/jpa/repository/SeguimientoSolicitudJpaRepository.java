package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.SeguimientoSolicitudEntity;

public interface SeguimientoSolicitudJpaRepository extends JpaRepository<SeguimientoSolicitudEntity, Long> {
    List<SeguimientoSolicitudEntity> findAllBySolicitud_IdOrderByFechaAsc(Long solicitudId);
}
