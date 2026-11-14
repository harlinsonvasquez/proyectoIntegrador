package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.ActividadEstudiantilEntity;

public interface ActividadEstudiantilJpaRepository extends JpaRepository<ActividadEstudiantilEntity, Long> {
    List<ActividadEstudiantilEntity> findAllByEstudiante_IdOrderByFechaInicioDesc(Long estudianteId);
}
