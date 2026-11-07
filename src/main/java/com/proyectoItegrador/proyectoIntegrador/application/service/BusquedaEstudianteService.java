package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoEstudiante;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.EstudianteJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.EstudianteResumenResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusquedaEstudianteService {
    private final EstudianteJpaRepository estudiantes;

    @Transactional(readOnly = true)
    public java.util.List<EstudianteResumenResponse> buscar(AuthenticatedUser actor,
            Long programaId, EstadoEstudiante estado, String texto) {
        if (actor.role() != RolNombre.DOCENTE && actor.role() != RolNombre.ADMINISTRADOR) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Solo docentes autorizados pueden buscar estudiantes.");
        }
        String query = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return estudiantes.findAll().stream()
                .filter(s -> programaId == null || s.getPrograma().getId().equals(programaId))
                .filter(s -> estado == null || s.getEstado() == estado)
                .filter(s -> query.isEmpty() || (s.getCodigoEstudiantil() + " " + s.getUsuario().getNombre()
                        + " " + s.getUsuario().getApellido() + " " + s.getUsuario().getCorreo())
                        .toLowerCase(Locale.ROOT).contains(query))
                .map(s -> new EstudianteResumenResponse(s.getId(), s.getCodigoEstudiantil(),
                        s.getUsuario().getNombre() + " " + s.getUsuario().getApellido(),
                        s.getUsuario().getCorreo(), s.getPrograma().getNombre(), s.getEstado()))
                .toList();
    }
}
