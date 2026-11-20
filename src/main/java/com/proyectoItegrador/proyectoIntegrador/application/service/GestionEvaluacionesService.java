package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.DocenteEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EvaluacionAsignaturaEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.MatriculaAsignaturaEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.NotaParcialEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OperacionAuditoria;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.AuditoriaCambioJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.DocenteJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.EvaluacionAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.MatriculaAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.NotaParcialJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.OfertaAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.UsuarioJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotaParcialDtos.CrearEvaluacionRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotaParcialDtos.EvaluacionResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotaParcialDtos.NotaParcialDocenteResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.NotaParcialDtos.RegistrarNotaParcialRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GestionEvaluacionesService {
    private static final Set<EstadoMatricula> ESTADOS_CALIFICABLES = Set.of(
            EstadoMatricula.MATRICULADA, EstadoMatricula.CURSANDO,
            EstadoMatricula.APROBADA, EstadoMatricula.REPROBADA);

    private final DocenteJpaRepository docentes;
    private final OfertaAsignaturaJpaRepository ofertas;
    private final MatriculaAsignaturaJpaRepository matriculas;
    private final EvaluacionAsignaturaJpaRepository evaluaciones;
    private final NotaParcialJpaRepository notas;
    private final UsuarioJpaRepository usuarios;
    private final AuditoriaCambioJpaRepository auditorias;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public List<EvaluacionResponse> evaluaciones(AuthenticatedUser actor, Long ofertaId) {
        DocenteEntity docente = docente(actor);
        asegurarOferta(ofertaId, docente.getId());
        return evaluaciones.findAllByOferta_IdOrderByCreadaEnAsc(ofertaId).stream()
                .map(this::respuesta).toList();
    }

    @Transactional
    public EvaluacionResponse crearEvaluacion(AuthenticatedUser actor, Long ofertaId,
            CrearEvaluacionRequest request) {
        DocenteEntity docente = docente(actor);
        var oferta = ofertas.bloquearPorIdYDocente(ofertaId, docente.getId())
                .orElseThrow(() -> noEncontrada("Oferta no encontrada para este docente."));
        BigDecimal asignado = evaluaciones.findAllByOferta_IdAndActivaTrueOrderByCreadaEnAsc(ofertaId).stream()
                .map(EvaluacionAsignaturaEntity::getPorcentaje)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (asignado.add(request.porcentaje()).compareTo(new BigDecimal("100.00")) > 0) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "El porcentaje acumulado de las evaluaciones no puede superar 100 %.");
        }
        EvaluacionAsignaturaEntity evaluacion = evaluaciones.saveAndFlush(new EvaluacionAsignaturaEntity(
                oferta, docente, request.nombre().trim(), limpiar(request.descripcion()),
                request.porcentaje(), request.fechaLimite()));
        auditar(actor, "evaluacion_asignatura", evaluacion.getId(), null,
                Map.of("ofertaId", ofertaId, "nombre", evaluacion.getNombre(),
                        "porcentaje", evaluacion.getPorcentaje().toPlainString()));
        return respuesta(evaluacion);
    }

    @Transactional(readOnly = true)
    public List<NotaParcialDocenteResponse> notasDeEvaluacion(AuthenticatedUser actor,
            Long ofertaId, Long evaluacionId) {
        DocenteEntity docente = docente(actor);
        asegurarOferta(ofertaId, docente.getId());
        EvaluacionAsignaturaEntity evaluacion = evaluacion(evaluacionId, ofertaId);
        Map<Long, NotaParcialEntity> notasPorMatricula = notas
                .findAllByEvaluacion_IdOrderByMatricula_Estudiante_Usuario_ApellidoAsc(evaluacionId).stream()
                .collect(Collectors.toMap(nota -> nota.getMatricula().getId(), Function.identity()));
        return matriculas.findAllByOfertaAsignatura_Id(ofertaId).stream()
                .filter(matricula -> ESTADOS_CALIFICABLES.contains(matricula.getEstado()))
                .map(matricula -> respuestaDocente(matricula,
                        notasPorMatricula.get(matricula.getId()), evaluacion.getPorcentaje()))
                .toList();
    }

    @Transactional
    public NotaParcialDocenteResponse registrarNota(AuthenticatedUser actor, Long ofertaId,
            Long evaluacionId, Long matriculaId, RegistrarNotaParcialRequest request) {
        DocenteEntity docente = docente(actor);
        asegurarOferta(ofertaId, docente.getId());
        EvaluacionAsignaturaEntity evaluacion = evaluacion(evaluacionId, ofertaId);
        MatriculaAsignaturaEntity matricula = matriculas.findByIdAndOfertaAsignatura_Id(matriculaId, ofertaId)
                .orElseThrow(() -> noEncontrada("Matrícula no encontrada en esta oferta."));
        if (!ESTADOS_CALIFICABLES.contains(matricula.getEstado())) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "No se puede calificar una matrícula cancelada o pendiente.");
        }

        NotaParcialEntity nota = notas.findByEvaluacion_IdAndMatricula_Id(evaluacionId, matriculaId)
                .orElse(null);
        Map<String, Object> anterior = nota == null ? null
                : Map.of("nota", nota.getNota().toPlainString(),
                        "observacion", nota.getObservacion() == null ? "" : nota.getObservacion());
        if (nota == null) {
            nota = new NotaParcialEntity(evaluacion, matricula, docente,
                    request.nota(), limpiar(request.observacion()));
        } else {
            nota.actualizar(docente, request.nota(), limpiar(request.observacion()));
        }
        nota = notas.saveAndFlush(nota);
        Map<String, Object> nuevos = Map.of("nota", nota.getNota().toPlainString(),
                "observacion", nota.getObservacion() == null ? "" : nota.getObservacion(),
                "evaluacionId", evaluacionId, "matriculaId", matriculaId);
        auditar(actor, "nota_parcial", nota.getId(), anterior, nuevos);
        recalcularNotaFinal(actor, matricula, ofertaId);
        return respuestaDocente(matricula, nota, evaluacion.getPorcentaje());
    }

    private EvaluacionAsignaturaEntity evaluacion(Long evaluacionId, Long ofertaId) {
        return evaluaciones.findByIdAndOferta_Id(evaluacionId, ofertaId)
                .orElseThrow(() -> noEncontrada("Evaluación no encontrada para esta oferta."));
    }

    private void asegurarOferta(Long ofertaId, Long docenteId) {
        if (ofertas.findByIdAndDocente_Id(ofertaId, docenteId).isEmpty()) {
            throw noEncontrada("Oferta no encontrada para este docente.");
        }
    }

    private EvaluacionResponse respuesta(EvaluacionAsignaturaEntity evaluacion) {
        return new EvaluacionResponse(evaluacion.getId(), evaluacion.getOferta().getId(),
                evaluacion.getNombre(), evaluacion.getDescripcion(), evaluacion.getPorcentaje(),
                evaluacion.getFechaLimite(), evaluacion.isActiva());
    }

    private NotaParcialDocenteResponse respuestaDocente(MatriculaAsignaturaEntity matricula,
            NotaParcialEntity nota, BigDecimal porcentaje) {
        var estudiante = matricula.getEstudiante();
        BigDecimal parcial = nota == null ? null : nota.getNota();
        return new NotaParcialDocenteResponse(matricula.getId(), estudiante.getId(),
                estudiante.getCodigoEstudiantil(), estudiante.getUsuario().getNombre() + " "
                        + estudiante.getUsuario().getApellido(), matricula.getEstado().name(), parcial,
                nota == null ? null : nota.getObservacion(), porcentaje, ponderado(parcial, porcentaje),
                matricula.getNotaFinal());
    }

    private void recalcularNotaFinal(AuthenticatedUser actor, MatriculaAsignaturaEntity matricula,
            Long ofertaId) {
        List<EvaluacionAsignaturaEntity> activas = evaluaciones
                .findAllByOferta_IdAndActivaTrueOrderByCreadaEnAsc(ofertaId);
        BigDecimal porcentajeTotal = activas.stream().map(EvaluacionAsignaturaEntity::getPorcentaje)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (porcentajeTotal.compareTo(new BigDecimal("100.00")) != 0) return;

        List<Long> ids = activas.stream().map(EvaluacionAsignaturaEntity::getId).toList();
        Map<Long, NotaParcialEntity> notasDeLaMatricula = notas.findAllByEvaluacion_IdIn(ids).stream()
                .filter(nota -> Objects.equals(nota.getMatricula().getId(), matricula.getId()))
                .collect(Collectors.toMap(nota -> nota.getEvaluacion().getId(), Function.identity()));
        if (activas.stream().anyMatch(evaluacion -> !notasDeLaMatricula.containsKey(evaluacion.getId()))) return;

        BigDecimal calculada = activas.stream()
                .map(evaluacion -> notasDeLaMatricula.get(evaluacion.getId()).getNota()
                        .multiply(evaluacion.getPorcentaje()).movePointLeft(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal anterior = matricula.getNotaFinal();
        if (anterior != null && anterior.compareTo(calculada) == 0) return;
        matricula.actualizarNotaFinal(calculada);
        Map<String, Object> valoresNuevos = Map.of("notaFinal", calculada.toPlainString(),
                "origen", "EVALUACIONES_PARCIALES");
        Map<String, Object> valoresAnteriores = Map.of("notaFinal",
                anterior == null ? "" : anterior.toPlainString());
        auditar(actor, "matricula_asignatura", matricula.getId(), valoresAnteriores, valoresNuevos);
    }

    private BigDecimal ponderado(BigDecimal nota, BigDecimal porcentaje) {
        return nota == null ? null : nota.multiply(porcentaje).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private void auditar(AuthenticatedUser actor, String entidad, Long id,
            Map<String, Object> anterior, Map<String, Object> nuevos) {
        usuarios.findById(actor.userId()).ifPresent(usuario -> auditorias.save(new AuditoriaCambioEntity(
                usuario, entidad, id.toString(), anterior == null ? OperacionAuditoria.INSERT : OperacionAuditoria.UPDATE,
                anterior == null ? null : mapper.valueToTree(anterior), mapper.valueToTree(nuevos))));
    }

    private DocenteEntity docente(AuthenticatedUser actor) {
        if (actor.role() != RolNombre.DOCENTE) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN,
                    "Solo el docente asignado puede gestionar las evaluaciones y notas de su oferta.");
        }
        DocenteEntity docente = docentes.findByUsuario_Id(actor.userId()).orElseThrow(() ->
                new ReglaNegocioException(HttpStatus.FORBIDDEN, "El usuario no tiene un perfil docente."));
        if (!docente.isActivo()) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "El perfil docente está inactivo.");
        }
        return docente;
    }

    private String limpiar(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ReglaNegocioException noEncontrada(String mensaje) {
        return new ReglaNegocioException(HttpStatus.NOT_FOUND, mensaje);
    }
}
