package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OfertaAsignaturaEntity;

public interface OfertaAsignaturaJpaRepository extends JpaRepository<OfertaAsignaturaEntity, Long> {
    List<OfertaAsignaturaEntity> findAllByPeriodo_Id(Long periodoId);
}
