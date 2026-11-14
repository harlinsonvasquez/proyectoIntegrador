package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoEstudiante;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.DocenteJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.EstudianteJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.MatriculaAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.OfertaAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.EstudianteResumenResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.ProgramaResponse;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusquedaEstudianteService {
    private final EstudianteJpaRepository estudiantes;
    private final DocenteJpaRepository docentes;
    private final MatriculaAsignaturaJpaRepository matriculas;
    private final OfertaAsignaturaJpaRepository ofertas;

    private static final Collection<EstadoMatricula> ESTADOS_ACTIVOS =
            java.util.List.of(EstadoMatricula.MATRICULADA, EstadoMatricula.CURSANDO);

    @Transactional(readOnly = true)
    public java.util.List<ProgramaResponse> programas(AuthenticatedUser actor) {
        if (actor.role() != RolNombre.DOCENTE) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN,
                    "Solo docentes autorizados pueden consultar sus programas.");
        }
        return docentes.findByUsuario_Id(actor.userId())
                .map(docente -> ofertas.findAllByDocente_Id(docente.getId()).stream()
                        .map(oferta -> oferta.getPlanAsignatura().getPlanEstudio().getPrograma())
                        .filter(programa -> programa.isActivo())
                        .collect(java.util.stream.Collectors.toMap(
                                programa -> programa.getId(),
                                programa -> new ProgramaResponse(programa.getId(), programa.getCodigo(),
                                        programa.getNombre(), programa.isActivo()),
                                (first, ignored) -> first, LinkedHashMap::new))
                        .values().stream().toList())
                .orElseGet(java.util.List::of);
    }

    @Transactional(readOnly = true)
    public java.util.List<EstudianteResumenResponse> buscar(AuthenticatedUser actor,
            Long programaId, EstadoEstudiante estado, String texto) {
        if (actor.role() != RolNombre.DOCENTE && actor.role() != RolNombre.ADMINISTRADOR) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Solo docentes autorizados pueden buscar estudiantes.");
        }
        String query = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        var candidatos = actor.role() == RolNombre.DOCENTE
                ? docentes.findByUsuario_Id(actor.userId())
                        .map(docente -> matriculas.findAllByOfertaAsignatura_Docente_IdAndEstadoIn(
                                docente.getId(), ESTADOS_ACTIVOS).stream()
                                .map(m -> m.getEstudiante().getId()).collect(java.util.stream.Collectors.toSet()))
                        .orElse(Set.of())
                : null;
        return estudiantes.findAll().stream()
                .filter(s -> candidatos == null || candidatos.contains(s.getId()))
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
