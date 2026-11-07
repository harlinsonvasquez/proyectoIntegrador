package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditoriaCambioJpaRepository extends JpaRepository<AuditoriaCambioEntity, Long> {
    @Query("""
            select a from AuditoriaCambioEntity a
            where (:entidad is null or a.entidad = :entidad)
              and (:registroId is null or a.registroId = :registroId)
              and (:usuarioId is null or a.usuario.id = :usuarioId)
            """)
    Page<AuditoriaCambioEntity> buscar(@Param("entidad") String entidad,
            @Param("registroId") String registroId, @Param("usuarioId") Long usuarioId, Pageable pageable);
}
