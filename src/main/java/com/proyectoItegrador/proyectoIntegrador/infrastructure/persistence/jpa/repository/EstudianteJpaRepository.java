package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstudianteEntity;

public interface EstudianteJpaRepository extends JpaRepository<EstudianteEntity, Long> {
    Optional<EstudianteEntity> findByUsuario_Id(Long usuarioId);
}
