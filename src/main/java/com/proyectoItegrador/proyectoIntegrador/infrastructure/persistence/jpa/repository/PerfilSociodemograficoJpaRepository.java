package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.PerfilSociodemograficoEntity;

public interface PerfilSociodemograficoJpaRepository extends JpaRepository<PerfilSociodemograficoEntity, Long> {
    Optional<PerfilSociodemograficoEntity> findByEstudiante_Id(Long estudianteId);
}
