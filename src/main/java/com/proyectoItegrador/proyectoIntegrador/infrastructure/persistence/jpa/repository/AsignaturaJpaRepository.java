package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AsignaturaEntity;

public interface AsignaturaJpaRepository extends JpaRepository<AsignaturaEntity, Long> {
    Optional<AsignaturaEntity> findByCodigoIgnoreCase(String codigo);
}
