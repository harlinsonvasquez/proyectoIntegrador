package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoItegrador.proyectoIntegrador.application.port.out.ConsultaAcademicaPort;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoEstudiante;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.MatriculaAsignaturaEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OperacionAuditoria;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.AuditoriaCambioJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.InscripcionEstudianteRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.IntentoAcademicoResponse;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InscripcionEstudianteService {
    private final ConsultaAcademicaPort persistence;
    private final AuditoriaCambioJpaRepository auditorias;
    private final ObjectMapper mapper;

    @Transactional
    public IntentoAcademicoResponse inscribir(AuthenticatedUser actor, InscripcionEstudianteRequest request) {
        if (actor.role() != RolNombre.ESTUDIANTE) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Solo un estudiante puede inscribirse a una asignatura.");
        }
        var estudiante = persistence.estudiantePorUsuario(actor.userId())
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND,
                        "No se encontró el perfil de estudiante."));
        if (estudiante.getEstado() != EstadoEstudiante.ACTIVO) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN,
                    "Solo los estudiantes activos pueden inscribirse a asignaturas.");
        }
        var periodo = persistence.periodo(request.periodoId())
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "Periodo académico no encontrado."));
        var oferta = persistence.ofertasDelProgramaEnPeriodo(estudiante.getPrograma().getId(), periodo.getId()).stream()
                .filter(item -> item.getId().equals(request.ofertaId()))
                .filter(item -> item.getPlanAsignatura().getAsignatura().isActivo())
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND,
                        "La oferta no está disponible para el programa y periodo seleccionados."));

        var intentos = persistence.matriculas(estudiante.getId());
        if (intentos.stream().anyMatch(item -> item.getOfertaAsignatura().getId().equals(oferta.getId()))) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya tienes una matrícula para este grupo.");
        }
        Long planAsignaturaId = oferta.getPlanAsignatura().getId();
        if (intentos.stream().anyMatch(item -> item.getOfertaAsignatura().getPlanAsignatura().getId()
                .equals(planAsignaturaId) && item.getEstado() == EstadoMatricula.APROBADA)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Ya aprobaste esta asignatura y no necesitas inscribirla de nuevo.");
        }
        if (intentos.stream().anyMatch(item -> item.getOfertaAsignatura().getPlanAsignatura().getId()
                .equals(planAsignaturaId)
                && item.getOfertaAsignatura().getPeriodo().getId().equals(periodo.getId())
                && (item.getEstado() == EstadoMatricula.MATRICULADA
                        || item.getEstado() == EstadoMatricula.CURSANDO
                        || item.getEstado() == EstadoMatricula.PENDIENTE))) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Ya tienes esta asignatura inscrita en otro grupo del mismo periodo.");
        }
        Set<Long> asignaturasAprobadas = new HashSet<>();
        intentos.stream().filter(item -> item.getEstado() == EstadoMatricula.APROBADA)
                .forEach(item -> asignaturasAprobadas.add(item.getOfertaAsignatura().getPlanAsignatura().getId()));
        var pendientes = persistence.prerrequisitos(oferta.getPlanAsignatura().getId()).stream()
                .filter(regla -> !asignaturasAprobadas.contains(regla.getPrerrequisito().getId()))
                .map(regla -> regla.getPrerrequisito().getAsignatura().getNombre()).sorted().toList();
        if (!pendientes.isEmpty()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Debes aprobar primero estos prerrequisitos: " + String.join(", ", pendientes) + ".");
        }
        Integer cupo = oferta.getCupo();
        long ocupados = persistence.matriculasActivasEnOferta(oferta.getId());
        if (cupo != null && ocupados >= cupo) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "El grupo ya no tiene cupos disponibles.");
        }

        var matricula = persistence.crearMatricula(new MatriculaAsignaturaEntity(
                estudiante, oferta, EstadoMatricula.MATRICULADA, null));
        var response = respuesta(matricula);
        auditorias.save(new AuditoriaCambioEntity(estudiante.getUsuario(), "matricula_asignatura",
                matricula.getId().toString(), OperacionAuditoria.INSERT, null,
                mapper.valueToTree(snapshot(response, estudiante.getId()))));
        return response;
    }

    private IntentoAcademicoResponse respuesta(MatriculaAsignaturaEntity matricula) {
        var oferta = matricula.getOfertaAsignatura();
        var planAsignatura = oferta.getPlanAsignatura();
        return new IntentoAcademicoResponse(matricula.getId(), planAsignatura.getId(),
                planAsignatura.getAsignatura().getCodigo(), planAsignatura.getAsignatura().getNombre(),
                oferta.getPeriodo().getCodigo(), oferta.getPeriodo().getNombre(), oferta.getGrupo(),
                matricula.getEstado(), matricula.getNotaFinal(), matricula.getFechaEstado());
    }

    private Map<String, Object> snapshot(IntentoAcademicoResponse response, Long estudianteId) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("estudianteId", estudianteId);
        values.put("planAsignaturaId", response.planAsignaturaId());
        values.put("codigoAsignatura", response.codigoAsignatura());
        values.put("estado", response.estado().name());
        values.put("notaFinal", response.notaFinal());
        values.put("periodo", response.periodo());
        values.put("grupo", response.grupo());
        return values;
    }
}
