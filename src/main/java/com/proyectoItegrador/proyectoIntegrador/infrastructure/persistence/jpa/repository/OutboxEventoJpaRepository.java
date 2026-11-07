package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OutboxEventoEntity;

public interface OutboxEventoJpaRepository extends JpaRepository<OutboxEventoEntity, Long> {
    List<OutboxEventoEntity> findTop100ByProcesadoEnIsNullOrderByCreadoEnAsc();
}
