package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.DocenteEntity;

public interface DocenteJpaRepository extends JpaRepository<DocenteEntity, Long> {
    Optional<DocenteEntity> findByUsuario_Id(Long usuarioId);
}
