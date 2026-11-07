package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.proyectoItegrador.proyectoIntegrador.application.port.out.SolicitudPersistencePort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoSolicitud;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ReporteSolicitudesResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class ReporteApplicationService {
    private final SolicitudPersistencePort persistence;

    @Transactional(readOnly = true)
    public ReporteSolicitudesResponse solicitudes(AuthenticatedUser actor,
            EstadoSolicitud filtroEstado, Long programaId, Long periodoId) {
        if (actor.role() != RolNombre.DOCENTE && actor.role() != RolNombre.ADMINISTRADOR) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Solo docentes autorizados pueden consultar reportes.");
        }
        var filtered = persistence.todasLasSolicitudes().stream()
                .filter(s -> filtroEstado == null || s.getEstado() == filtroEstado)
                .filter(s -> programaId == null || s.getEstudiante().getPrograma().getId().equals(programaId))
                .filter(s -> periodoId == null || s.getMatricula().getOfertaAsignatura().getPeriodo().getId().equals(periodoId))
                .toList();
        Map<String, Long> estados = new LinkedHashMap<>();
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            estados.put(estado.name(), filtered.stream().filter(s -> s.getEstado() == estado).count());
        }
        Map<String, Long> periodos = new LinkedHashMap<>();
        Map<String, Long> programas = new LinkedHashMap<>();
        filtered.forEach(s -> {
            periodos.merge(s.getMatricula().getOfertaAsignatura().getPeriodo().getNombre(), 1L, Long::sum);
            programas.merge(s.getEstudiante().getPrograma().getNombre(), 1L, Long::sum);
        });
        return new ReporteSolicitudesResponse(filtered.size(), estados, periodos, programas);
    }
}
