package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.ProgramaEntity;

public interface ProgramaJpaRepository extends JpaRepository<ProgramaEntity, Long> {
}
