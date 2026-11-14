package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoSolicitud;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.SolicitudCancelacionEntity;

public interface SolicitudCancelacionJpaRepository extends JpaRepository<SolicitudCancelacionEntity, Long> {
    List<SolicitudCancelacionEntity> findAllByEstudiante_IdOrderByFechaCreacionDesc(Long estudianteId);
    List<SolicitudCancelacionEntity> findAllByEstadoOrderByFechaCreacionAsc(EstadoSolicitud estado);
    Optional<SolicitudCancelacionEntity> findByIdAndEstudiante_Id(Long id, Long estudianteId);
    long countByEstado(EstadoSolicitud estado);
}
