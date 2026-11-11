package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.ActividadEstudiantilJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.MatriculaAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.PeriodoAcademicoJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.ReporteDesempenoResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReporteAcademicoService {
    private final MatriculaAsignaturaJpaRepository matriculas;
    private final ActividadEstudiantilJpaRepository actividades;
    private final PeriodoAcademicoJpaRepository periodos;

    @Transactional(readOnly = true)
    public ReporteDesempenoResponse desempeno(AuthenticatedUser actor, Long programaId, Long periodoId) {
        exigirDocente(actor);
        if (periodoId != null && !periodos.existsById(periodoId)) {
            throw new ReglaNegocioException(HttpStatus.NOT_FOUND, "Periodo académico no encontrado.");
        }
        var rows = (programaId == null ? matriculas.findAll()
                : matriculas.findAllByEstudiante_Programa_Id(programaId)).stream()
                .filter(m -> periodoId == null || m.getOfertaAsignatura().getPeriodo().getId().equals(periodoId))
                .toList();
        long aprobadas = rows.stream().filter(m -> m.getEstado() == EstadoMatricula.APROBADA).count();
        long reprobadas = rows.stream().filter(m -> m.getEstado() == EstadoMatricula.REPROBADA).count();
        long cursando = rows.stream().filter(m -> m.getEstado() == EstadoMatricula.CURSANDO
                || m.getEstado() == EstadoMatricula.MATRICULADA).count();
        BigDecimal average = rows.stream().map(MatriculaAsignaturaEntity::getNotaFinal).filter(n -> n != null)
                .mapToDouble(BigDecimal::doubleValue).average().stream()
                .mapToObj(BigDecimal::valueOf).findFirst().orElse(null);
        return new ReporteDesempenoResponse(rows.size(), aprobadas, reprobadas, cursando, average);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> participacion(AuthenticatedUser actor, Long programaId, Long periodoId) {
        exigirDocente(actor);
        var periodo = periodoId == null ? null : periodos.findById(periodoId)
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "Periodo académico no encontrado."));
        Map<String, Long> counts = new LinkedHashMap<>();
        for (CategoriaActividad category : CategoriaActividad.values()) counts.put(category.name(), 0L);
        actividades.findAll().stream()
                .filter(a -> programaId == null || a.getEstudiante().getPrograma().getId().equals(programaId))
                .filter(a -> periodo == null || coincidePeriodo(a.getFechaInicio(), a.getFechaFin(),
                        periodo.getFechaInicio(), periodo.getFechaFin()))
                .forEach(a -> counts.merge(a.getCategoria().name(), 1L, Long::sum));
        return counts;
    }

    private boolean coincidePeriodo(LocalDate fechaInicio, LocalDate fechaFin,
            LocalDate periodoInicio, LocalDate periodoFin) {
        LocalDate inicioActividad = fechaInicio == null ? fechaFin : fechaInicio;
        LocalDate finActividad = fechaFin == null ? fechaInicio : fechaFin;
        return inicioActividad != null && finActividad != null
                && !inicioActividad.isAfter(periodoFin) && !finActividad.isBefore(periodoInicio);
    }

    private void exigirDocente(AuthenticatedUser actor) {
        if (actor.role() != RolNombre.DOCENTE && actor.role() != RolNombre.ADMINISTRADOR)
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Solo docentes autorizados pueden consultar reportes.");
    }
}
