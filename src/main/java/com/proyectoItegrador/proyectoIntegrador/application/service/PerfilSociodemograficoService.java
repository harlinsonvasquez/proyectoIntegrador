package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoItegrador.proyectoIntegrador.application.port.out.PerfilSociodemograficoPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ActualizarPerfilSociodemograficoRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.PerfilSociodemograficoResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilSociodemograficoService {
    private final PerfilSociodemograficoPort persistence;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public PerfilSociodemograficoResponse mio(AuthenticatedUser actor) {
        exigir(actor, RolNombre.ESTUDIANTE);
        var student = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> notFound("Perfil de estudiante no encontrado."));
        return persistence.perfil(student.getId()).map(this::respuesta).orElse(null);
    }

    @Transactional
    public PerfilSociodemograficoResponse actualizar(AuthenticatedUser actor,
            ActualizarPerfilSociodemograficoRequest request) {
        exigir(actor, RolNombre.ESTUDIANTE);
        var student = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> notFound("Perfil de estudiante no encontrado."));
        var existing = persistence.perfil(student.getId());
        Map<String, Object> before = existing.map(this::valores).orElse(null);
        var profile = existing.orElseGet(() -> new PerfilSociodemograficoEntity(
                student, null, null, null, false, null));
        profile.actualizar(request.municipioResidencia(), request.estrato(), request.situacionLaboral(),
                request.requiereApoyo(), request.observacion());
        profile = persistence.guardar(profile);
        persistence.guardarAuditoria(new AuditoriaCambioEntity(student.getUsuario(),
                "perfil_sociodemografico", profile.getId().toString(),
                before == null ? OperacionAuditoria.INSERT : OperacionAuditoria.UPDATE,
                before == null ? null : mapper.valueToTree(before), mapper.valueToTree(valores(profile))));
        return respuesta(profile);
    }

    @Transactional(readOnly = true)
    public PerfilSociodemograficoResponse deEstudiante(AuthenticatedUser actor, Long id) {
        exigir(actor, RolNombre.DOCENTE, RolNombre.ADMINISTRADOR);
        persistence.estudiante(id).orElseThrow(() -> notFound("Estudiante no encontrado."));
        return persistence.perfil(id).map(this::respuesta).orElse(null);
    }

    private Map<String, Object> valores(PerfilSociodemograficoEntity p) {
        return Map.of("municipioResidencia", p.getMunicipioResidencia() == null ? "" : p.getMunicipioResidencia(),
                "estrato", p.getEstrato() == null ? 0 : p.getEstrato(),
                "situacionLaboral", p.getSituacionLaboral() == null ? "" : p.getSituacionLaboral(),
                "requiereApoyo", p.isRequiereApoyo(),
                "observacion", p.getObservacion() == null ? "" : p.getObservacion());
    }
    private PerfilSociodemograficoResponse respuesta(PerfilSociodemograficoEntity p) {
        return new PerfilSociodemograficoResponse(p.getEstudiante().getId(), p.getMunicipioResidencia(),
                p.getEstrato(), p.getSituacionLaboral(), p.isRequiereApoyo(), p.getObservacion(), p.getActualizadoEn());
    }
    private void exigir(AuthenticatedUser actor, RolNombre... roles) {
        for (RolNombre role : roles) if (actor.role() == role) return;
        throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Tu rol no permite realizar esta operación.");
    }
    private ReglaNegocioException notFound(String message) {
        return new ReglaNegocioException(HttpStatus.NOT_FOUND, message);
    }
}
