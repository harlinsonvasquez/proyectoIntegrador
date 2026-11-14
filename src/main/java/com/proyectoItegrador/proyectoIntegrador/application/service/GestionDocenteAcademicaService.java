package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.AuditoriaCambioEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.DocenteEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.MatriculaAsignaturaEntity;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OperacionAuditoria;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.AuditoriaCambioJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.DocenteJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.MatriculaAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.OfertaAsignaturaJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.UsuarioJpaRepository;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DocenteDtos.CalificacionDocenteRequest;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DocenteDtos.MatriculaDocenteResponse;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.DocenteDtos.OfertaDocenteResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GestionDocenteAcademicaService {
    private static final Set<EstadoMatricula> ESTADOS_EDITABLES = Set.of(
            EstadoMatricula.MATRICULADA, EstadoMatricula.CURSANDO,
            EstadoMatricula.APROBADA, EstadoMatricula.REPROBADA);
    private static final Set<EstadoMatricula> ESTADOS_EN_CURSO = Set.of(
            EstadoMatricula.MATRICULADA, EstadoMatricula.CURSANDO);

    private final DocenteJpaRepository docentes;
    private final OfertaAsignaturaJpaRepository ofertas;
    private final MatriculaAsignaturaJpaRepository matriculas;
    private final UsuarioJpaRepository usuarios;
    private final AuditoriaCambioJpaRepository auditorias;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public List<OfertaDocenteResponse> ofertas(AuthenticatedUser actor) {
        DocenteEntity docente = docente(actor);
        return ofertas.findAllByDocente_Id(docente.getId()).stream()
                .map(oferta -> {
                    var planAsignatura = oferta.getPlanAsignatura();
                    long estudiantes = matriculas.findAllByOfertaAsignatura_Id(oferta.getId()).stream()
                            .filter(m -> ESTADOS_EN_CURSO.contains(m.getEstado())).count();
                    return new OfertaDocenteResponse(oferta.getId(),
                            planAsignatura.getAsignatura().getCodigo(), planAsignatura.getAsignatura().getNombre(),
                            planAsignatura.getPlanEstudio().getNombre(), oferta.getPeriodo().getNombre(),
                            oferta.getGrupo(), estudiantes);
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<MatriculaDocenteResponse> matriculas(AuthenticatedUser actor, Long ofertaId) {
        DocenteEntity docente = docente(actor);
        if (ofertas.findByIdAndDocente_Id(ofertaId, docente.getId()).isEmpty()) {
            throw new ReglaNegocioException(HttpStatus.NOT_FOUND, "Oferta no encontrada para este docente.");
        }
        return matriculas.findAllByOfertaAsignatura_Id(ofertaId).stream()
                .map(this::respuesta).toList();
    }

    @Transactional
    public MatriculaDocenteResponse actualizarCalificacion(AuthenticatedUser actor, Long matriculaId,
            CalificacionDocenteRequest request) {
        DocenteEntity docente = docente(actor);
        MatriculaAsignaturaEntity matricula = matriculas
                .findByIdAndOfertaAsignatura_Docente_Id(matriculaId, docente.getId())
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND,
                        "Matrícula no encontrada para las asignaturas de este docente."));
        validarCalificacion(matricula, request);

        var anterior = Map.of("estado", matricula.getEstado().name(),
                "notaFinal", matricula.getNotaFinal() == null ? "" : matricula.getNotaFinal().toPlainString());
        matricula.actualizar(request.estado(), request.notaFinal());
        var nuevos = Map.of("estado", matricula.getEstado().name(),
                "notaFinal", matricula.getNotaFinal() == null ? "" : matricula.getNotaFinal().toPlainString());
        usuarios.findById(actor.userId()).ifPresent(usuario -> auditorias.save(new AuditoriaCambioEntity(
                usuario, "matricula_asignatura", matricula.getId().toString(), OperacionAuditoria.UPDATE,
                mapper.valueToTree(anterior), mapper.valueToTree(nuevos))));
        return respuesta(matricula);
    }

    private void validarCalificacion(MatriculaAsignaturaEntity matricula, CalificacionDocenteRequest request) {
        if (matricula.getEstado() == EstadoMatricula.CANCELADA) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "No se puede calificar una matrícula cancelada.");
        }
        if (!ESTADOS_EDITABLES.contains(matricula.getEstado())) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Esta matrícula no está habilitada para gestión docente.");
        }
        if (!ESTADOS_EDITABLES.contains(request.estado())) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "El docente solo puede usar estados matriculada, cursando, aprobada o reprobada.");
        }
        BigDecimal nota = request.notaFinal();
        if (nota != null && (nota.compareTo(BigDecimal.ZERO) < 0
                || nota.compareTo(new BigDecimal("5.00")) > 0 || nota.scale() > 2)) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "La nota debe estar entre 0 y 5 con máximo dos decimales.");
        }
        if ((request.estado() == EstadoMatricula.APROBADA || request.estado() == EstadoMatricula.REPROBADA)
                && nota == null) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "Registra la nota final antes de marcar la matrícula como aprobada o reprobada.");
        }
    }

    private MatriculaDocenteResponse respuesta(MatriculaAsignaturaEntity matricula) {
        var estudiante = matricula.getEstudiante();
        var oferta = matricula.getOfertaAsignatura();
        return new MatriculaDocenteResponse(matricula.getId(), estudiante.getId(),
                estudiante.getCodigoEstudiantil(), estudiante.getUsuario().getNombre() + " "
                        + estudiante.getUsuario().getApellido(), matricula.getEstado(), matricula.getNotaFinal(),
                oferta.getPeriodo().getNombre(), oferta.getGrupo());
    }

    private DocenteEntity docente(AuthenticatedUser actor) {
        if (actor.role() != RolNombre.DOCENTE) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Solo el docente asignado puede gestionar sus notas.");
        }
        DocenteEntity docente = docentes.findByUsuario_Id(actor.userId()).orElseThrow(() ->
                new ReglaNegocioException(HttpStatus.FORBIDDEN, "El usuario no tiene un perfil docente activo."));
        if (!docente.isActivo()) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "El perfil docente está inactivo.");
        }
        return docente;
    }
}
