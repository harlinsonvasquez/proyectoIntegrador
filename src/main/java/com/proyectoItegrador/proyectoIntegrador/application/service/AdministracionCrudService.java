package com.proyectoItegrador.proyectoIntegrador.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.config.AuthenticatedUser;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.repository.*;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto.AdminDtos.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class AdministracionCrudService {
    private static final Logger log = LoggerFactory.getLogger(AdministracionCrudService.class);
    private final UsuarioJpaRepository usuarios;
    private final RolJpaRepository roles;
    private final EstudianteJpaRepository estudiantes;
    private final DocenteJpaRepository docentes;
    private final ProgramaJpaRepository programas;
    private final PlanEstudioJpaRepository planes;
    private final AsignaturaJpaRepository asignaturas;
    private final PlanAsignaturaJpaRepository planAsignaturas;
    private final PrerrequisitoJpaRepository prerrequisitos;
    private final PeriodoAcademicoJpaRepository periodos;
    private final OfertaAsignaturaJpaRepository ofertas;
    private final MatriculaAsignaturaJpaRepository matriculas;
    private final ActividadEstudiantilJpaRepository actividades;
    private final PerfilSociodemograficoJpaRepository perfiles;
    private final AuditoriaCambioJpaRepository auditorias;
    private final ObjectProvider<FirebaseAuth> firebaseAuth;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> usuarios() {
        return usuarios.findAll().stream().map(this::usuarioResponse).toList();
    }

    @Transactional
    public UsuarioResponse cambiarRol(AuthenticatedUser actor, Long id, CambiarRolRequest request) {
        if (actor.userId().equals(id))
            throw regla(HttpStatus.CONFLICT, "No puedes cambiar tu propio rol.");

        var usuario = buscar(usuarios, id, "Usuario no encontrado.");
        RolNombre rolAnterior = usuario.getRol().getNombre();
        RolNombre rolNuevo = request.rol();

        if (rolAnterior != rolNuevo && rolAnterior == RolNombre.ADMINISTRADOR
                && rolNuevo != RolNombre.ADMINISTRADOR
                && usuarios.countByRol_NombreAndActivoTrue(RolNombre.ADMINISTRADOR) <= 1)
            throw regla(HttpStatus.CONFLICT, "No se puede cambiar el rol del último administrador activo.");

        if (rolNuevo == RolNombre.ESTUDIANTE && estudiantes.findByUsuario_Id(id).isEmpty())
            throw regla(HttpStatus.CONFLICT,
                    "El usuario no tiene perfil de estudiante; créalo mediante el flujo de registro de estudiante.");

        if (rolNuevo == RolNombre.DOCENTE) {
            var docente = docentes.findByUsuario_Id(id);
            if (docente.isEmpty()) {
                var creado = docentes.save(new DocenteEntity(usuario, opcional(request.codigoDocente())));
                audit(actor, "docente", creado.getId(), OperacionAuditoria.INSERT, null, response(creado));
            } else if (usuario.isActivo() && !docente.get().isActivo()) {
                var oldDocente = response(docente.get());
                docente.get().actualizar(
                        opcional(request.codigoDocente()) == null
                                ? docente.get().getCodigoDocente() : opcional(request.codigoDocente()),
                        true);
                audit(actor, "docente", docente.get().getId(), OperacionAuditoria.UPDATE,
                        oldDocente, response(docente.get()));
            }
        }

        if (rolAnterior == rolNuevo) return usuarioResponse(usuario);

        var old = usuarioResponse(usuario);
        usuario.cambiarRol(rol(rolNuevo));
        var updated = usuarioResponse(usuario);
        audit(actor, "usuario", id, OperacionAuditoria.UPDATE, old, updated);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<ProgramaResponse> programas() { return programas.findAll().stream().map(this::response).toList(); }
    @Transactional(readOnly = true)
    public ProgramaResponse programa(Long id) { return response(buscar(programas, id, "Programa no encontrado.")); }
    @Transactional
    public ProgramaResponse crearPrograma(AuthenticatedUser actor, ProgramaRequest r) {
        var e = programas.save(new ProgramaEntity(limpio(r.codigo()), limpio(r.nombre()), r.activo()));
        audit(actor, "programa", e.getId(), OperacionAuditoria.INSERT, null, response(e));
        return response(e);
    }
    @Transactional
    public ProgramaResponse actualizarPrograma(AuthenticatedUser actor, Long id, ProgramaRequest r) {
        var e = buscar(programas, id, "Programa no encontrado.");
        var old = response(e);
        e.actualizar(limpio(r.codigo()), limpio(r.nombre()), r.activo());
        audit(actor, "programa", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }
    @Transactional
    public void eliminarPrograma(AuthenticatedUser actor, Long id) {
        var e = buscar(programas, id, "Programa no encontrado.");
        var old = response(e);
        e.desactivar();
        audit(actor, "programa", id, OperacionAuditoria.UPDATE, old, response(e));
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> planes() { return planes.findAll().stream().map(this::response).toList(); }
    @Transactional(readOnly = true)
    public PlanResponse plan(Long id) { return response(buscar(planes, id, "Plan de estudio no encontrado.")); }
    @Transactional
    public PlanResponse crearPlan(AuthenticatedUser actor, PlanRequest r) {
        validarFechas(r.fechaInicio(), r.fechaFin());
        var e = planes.save(new PlanEstudioEntity(programaEntity(r.programaId()), limpio(r.codigo()),
                limpio(r.nombre()), limpio(r.version()), r.fechaInicio(), r.fechaFin(), r.activo()));
        audit(actor, "plan_estudio", e.getId(), OperacionAuditoria.INSERT, null, response(e));
        return response(e);
    }
    @Transactional
    public PlanResponse actualizarPlan(AuthenticatedUser actor, Long id, PlanRequest r) {
        validarFechas(r.fechaInicio(), r.fechaFin());
        var e = buscar(planes, id, "Plan de estudio no encontrado.");
        var old = response(e);
        e.actualizar(programaEntity(r.programaId()), limpio(r.codigo()), limpio(r.nombre()),
                limpio(r.version()), r.fechaInicio(), r.fechaFin(), r.activo());
        audit(actor, "plan_estudio", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }
    @Transactional
    public void eliminarPlan(AuthenticatedUser actor, Long id) {
        var e = buscar(planes, id, "Plan de estudio no encontrado.");
        var old = response(e);
        e.desactivar();
        audit(actor, "plan_estudio", id, OperacionAuditoria.UPDATE, old, response(e));
    }

    @Transactional(readOnly = true)
    public List<AsignaturaResponse> asignaturas() {
        return asignaturas.findAll().stream().map(this::response).toList();
    }
    @Transactional(readOnly = true)
    public AsignaturaResponse asignatura(Long id) {
        return response(buscar(asignaturas, id, "Asignatura no encontrada."));
    }
    @Transactional
    public AsignaturaResponse crearAsignatura(AuthenticatedUser actor, AsignaturaRequest r) {
        var e = asignaturas.save(new AsignaturaEntity(limpio(r.codigo()), limpio(r.nombre()), r.creditos(), r.activo()));
        audit(actor, "asignatura", e.getId(), OperacionAuditoria.INSERT, null, response(e));
        return response(e);
    }
    @Transactional
    public AsignaturaResponse actualizarAsignatura(AuthenticatedUser actor, Long id, AsignaturaRequest r) {
        var e = buscar(asignaturas, id, "Asignatura no encontrada.");
        var old = response(e);
        e.actualizar(limpio(r.codigo()), limpio(r.nombre()), r.creditos(), r.activo());
        audit(actor, "asignatura", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }
    @Transactional
    public void eliminarAsignatura(AuthenticatedUser actor, Long id) {
        var e = buscar(asignaturas, id, "Asignatura no encontrada.");
        var old = response(e);
        e.desactivar();
        audit(actor, "asignatura", id, OperacionAuditoria.UPDATE, old, response(e));
    }

    @Transactional(readOnly = true)
    public List<PlanAsignaturaResponse> planAsignaturas() {
        return planAsignaturas.findAll().stream().map(this::response).toList();
    }
    @Transactional(readOnly = true)
    public PlanAsignaturaResponse planAsignatura(Long id) {
        return response(buscar(planAsignaturas, id, "Asignatura del plan no encontrada."));
    }
    @Transactional
    public PlanAsignaturaResponse crearPlanAsignatura(AuthenticatedUser actor, PlanAsignaturaRequest r) {
        var e = planAsignaturas.save(new PlanAsignaturaEntity(planEntity(r.planEstudioId()),
                buscar(asignaturas, r.asignaturaId(), "Asignatura no encontrada."),
                r.semestreSugerido(), r.obligatoria()));
        audit(actor, "plan_asignatura", e.getId(), OperacionAuditoria.INSERT, null, response(e));
        return response(e);
    }
    @Transactional
    public PlanAsignaturaResponse actualizarPlanAsignatura(AuthenticatedUser actor, Long id,
            PlanAsignaturaRequest r) {
        var e = buscar(planAsignaturas, id, "Asignatura del plan no encontrada.");
        var old = response(e);
        e.actualizar(planEntity(r.planEstudioId()), buscar(asignaturas, r.asignaturaId(), "Asignatura no encontrada."),
                r.semestreSugerido(), r.obligatoria());
        audit(actor, "plan_asignatura", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }
    @Transactional
    public void eliminarPlanAsignatura(AuthenticatedUser actor, Long id) {
        var e = buscar(planAsignaturas, id, "Asignatura del plan no encontrada.");
        var old = response(e);
        planAsignaturas.delete(e);
        audit(actor, "plan_asignatura", id, OperacionAuditoria.DELETE, old, null);
    }

    @Transactional(readOnly = true)
    public List<PrerrequisitoResponse> prerrequisitos() {
        return prerrequisitos.findAll().stream()
                .map(p -> new PrerrequisitoResponse(p.getPlanAsignatura().getId(), p.getPrerrequisito().getId()))
                .toList();
    }
    @Transactional(readOnly = true)
    public PrerrequisitoResponse prerrequisito(Long planId, Long requisitoId) {
        var e = buscar(prerrequisitos, new PrerrequisitoId(planId, requisitoId), "Prerrequisito no encontrado.");
        return new PrerrequisitoResponse(e.getPlanAsignatura().getId(), e.getPrerrequisito().getId());
    }
    @Transactional
    public PrerrequisitoResponse crearPrerrequisito(AuthenticatedUser actor, PrerrequisitoRequest r) {
        if (r.planAsignaturaId().equals(r.prerrequisitoPlanAsignaturaId()))
            throw regla(HttpStatus.BAD_REQUEST, "Una asignatura no puede ser prerrequisito de sí misma.");
        var e = new PrerrequisitoEntity(planAsignaturaEntity(r.planAsignaturaId()),
                planAsignaturaEntity(r.prerrequisitoPlanAsignaturaId()));
        prerrequisitos.save(e);
        var response = new PrerrequisitoResponse(r.planAsignaturaId(), r.prerrequisitoPlanAsignaturaId());
        audit(actor, "prerrequisito", r.planAsignaturaId() + ":" + r.prerrequisitoPlanAsignaturaId(),
                OperacionAuditoria.INSERT, null, response);
        return response;
    }
    @Transactional
    public void eliminarPrerrequisito(AuthenticatedUser actor, Long planId, Long requisitoId) {
        var id = new PrerrequisitoId(planId, requisitoId);
        var e = buscar(prerrequisitos, id, "Prerrequisito no encontrado.");
        prerrequisitos.delete(e);
        audit(actor, "prerrequisito", planId + ":" + requisitoId, OperacionAuditoria.DELETE,
                new PrerrequisitoResponse(planId, requisitoId), null);
    }

    @Transactional(readOnly = true)
    public List<PeriodoResponse> periodos() { return periodos.findAll().stream().map(this::response).toList(); }
    @Transactional(readOnly = true)
    public PeriodoResponse periodo(Long id) { return response(buscar(periodos, id, "Periodo no encontrado.")); }
    @Transactional
    public PeriodoResponse crearPeriodo(AuthenticatedUser actor, PeriodoRequest r) {
        validarFechas(r.fechaInicio(), r.fechaFin());
        var e = periodos.save(new PeriodoAcademicoEntity(limpio(r.codigo()), limpio(r.nombre()),
                r.fechaInicio(), r.fechaFin()));
        audit(actor, "periodo_academico", e.getId(), OperacionAuditoria.INSERT, null, response(e));
        return response(e);
    }
    @Transactional
    public PeriodoResponse actualizarPeriodo(AuthenticatedUser actor, Long id, PeriodoRequest r) {
        validarFechas(r.fechaInicio(), r.fechaFin());
        var e = buscar(periodos, id, "Periodo no encontrado.");
        var old = response(e);
        e.actualizar(limpio(r.codigo()), limpio(r.nombre()), r.fechaInicio(), r.fechaFin());
        audit(actor, "periodo_academico", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }
    @Transactional
    public void eliminarPeriodo(AuthenticatedUser actor, Long id) {
        var e = buscar(periodos, id, "Periodo no encontrado.");
        var old = response(e);
        periodos.delete(e);
        audit(actor, "periodo_academico", id, OperacionAuditoria.DELETE, old, null);
    }

    @Transactional(readOnly = true)
    public List<OfertaResponse> ofertas() { return ofertas.findAll().stream().map(this::response).toList(); }
    @Transactional(readOnly = true)
    public OfertaResponse oferta(Long id) { return response(buscar(ofertas, id, "Oferta no encontrada.")); }
    @Transactional
    public OfertaResponse crearOferta(AuthenticatedUser actor, OfertaRequest r) {
        var e = ofertas.save(new OfertaAsignaturaEntity(planAsignaturaEntity(r.planAsignaturaId()),
                periodoEntity(r.periodoId()), limpio(r.grupo()), r.cupo(), docenteActivo(r.docenteId())));
        audit(actor, "oferta_asignatura", e.getId(), OperacionAuditoria.INSERT, null, response(e));
        return response(e);
    }
    @Transactional
    public OfertaResponse actualizarOferta(AuthenticatedUser actor, Long id, OfertaRequest r) {
        var e = buscar(ofertas, id, "Oferta no encontrada.");
        var old = response(e);
        e.actualizar(planAsignaturaEntity(r.planAsignaturaId()), periodoEntity(r.periodoId()),
                limpio(r.grupo()), r.cupo(), docenteActivo(r.docenteId()));
        audit(actor, "oferta_asignatura", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }
    @Transactional
    public void eliminarOferta(AuthenticatedUser actor, Long id) {
        var e = buscar(ofertas, id, "Oferta no encontrada.");
        var old = response(e);
        ofertas.delete(e);
        audit(actor, "oferta_asignatura", id, OperacionAuditoria.DELETE, old, null);
    }

    @Transactional(readOnly = true)
    public List<EstudianteResponse> estudiantes() { return estudiantes.findAll().stream().map(this::response).toList(); }
    @Transactional(readOnly = true)
    public EstudianteResponse estudiante(Long id) { return response(buscar(estudiantes, id, "Estudiante no encontrado.")); }
    @Transactional
    public EstudianteResponse crearEstudiante(AuthenticatedUser actor, CrearEstudianteRequest r) {
        return crearCuenta(r.correo(), r.contrasena(), r.nombre(), r.apellido(), () -> {
            var user = usuarios.saveAndFlush(new UsuarioEntity(rol(RolNombre.ESTUDIANTE),
                    correo(r.correo()), limpio(r.nombre()), limpio(r.apellido())));
            var e = estudiantes.saveAndFlush(new EstudianteEntity(user, programaEntity(r.programaId()),
                    limpio(r.codigoEstudiantil()), r.fechaIngreso(), EstadoEstudiante.ACTIVO));
            audit(actor, "usuario", user.getId(), OperacionAuditoria.INSERT, null, usuarioResponse(user));
            audit(actor, "estudiante", e.getId(), OperacionAuditoria.INSERT, null, response(e));
            return response(e);
        });
    }
    @Transactional
    public EstudianteResponse actualizarEstudiante(AuthenticatedUser actor, Long id, ActualizarEstudianteRequest r) {
        var e = buscar(estudiantes, id, "Estudiante no encontrado.");
        var old = response(e);
        syncAccount(e.getUsuario(), r.estado() != EstadoEstudiante.INACTIVO, actor);
        e.getUsuario().actualizarDatosPersonales(limpio(r.nombre()), limpio(r.apellido()));
        e.actualizar(programaEntity(r.programaId()), limpio(r.codigoEstudiantil()), r.fechaIngreso(), r.estado());
        audit(actor, "estudiante", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }
    @Transactional
    public void eliminarEstudiante(AuthenticatedUser actor, Long id) {
        var e = buscar(estudiantes, id, "Estudiante no encontrado.");
        var old = response(e);
        syncAccount(e.getUsuario(), false, actor);
        e.inactivar();
        audit(actor, "estudiante", id, OperacionAuditoria.UPDATE, old, response(e));
    }

    @Transactional(readOnly = true)
    public List<DocenteResponse> docentes() { return docentes.findAll().stream().map(this::response).toList(); }
    @Transactional(readOnly = true)
    public DocenteResponse docente(Long id) { return response(buscar(docentes, id, "Docente no encontrado.")); }
    @Transactional
    public DocenteResponse crearDocente(AuthenticatedUser actor, CrearDocenteRequest r) {
        return crearCuenta(r.correo(), r.contrasena(), r.nombre(), r.apellido(), () -> {
            var user = usuarios.saveAndFlush(new UsuarioEntity(rol(RolNombre.DOCENTE),
                    correo(r.correo()), limpio(r.nombre()), limpio(r.apellido())));
            var e = docentes.saveAndFlush(new DocenteEntity(user, opcional(r.codigoDocente())));
            audit(actor, "usuario", user.getId(), OperacionAuditoria.INSERT, null, usuarioResponse(user));
            audit(actor, "docente", e.getId(), OperacionAuditoria.INSERT, null, response(e));
            return response(e);
        });
    }
    @Transactional
    public DocenteResponse actualizarDocente(AuthenticatedUser actor, Long id, ActualizarDocenteRequest r) {
        var e = buscar(docentes, id, "Docente no encontrado.");
        var old = response(e);
        syncAccount(e.getUsuario(), r.activo(), actor);
        e.getUsuario().actualizarDatosPersonales(limpio(r.nombre()), limpio(r.apellido()));
        e.actualizar(opcional(r.codigoDocente()), r.activo());
        audit(actor, "docente", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }
    @Transactional
    public void eliminarDocente(AuthenticatedUser actor, Long id) {
        var e = buscar(docentes, id, "Docente no encontrado.");
        var old = response(e);
        syncAccount(e.getUsuario(), false, actor);
        e.inactivar();
        audit(actor, "docente", id, OperacionAuditoria.UPDATE, old, response(e));
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> matriculas() { return matriculas.findAll().stream().map(this::response).toList(); }
    @Transactional(readOnly = true)
    public MatriculaResponse matricula(Long id) { return response(buscar(matriculas, id, "Matrícula no encontrada.")); }
    @Transactional
    public MatriculaResponse crearMatricula(AuthenticatedUser actor, MatriculaRequest r) {
        validarNota(r.notaFinal());
        if (r.estado() == EstadoMatricula.CANCELADA)
            throw regla(HttpStatus.BAD_REQUEST, "La cancelación debe tramitarse mediante una solicitud.");
        var e = matriculas.save(new MatriculaAsignaturaEntity(
                buscar(estudiantes, r.estudianteId(), "Estudiante no encontrado."),
                buscar(ofertas, r.ofertaAsignaturaId(), "Oferta no encontrada."), r.estado(), r.notaFinal()));
        audit(actor, "matricula_asignatura", e.getId(), OperacionAuditoria.INSERT, null, response(e));
        return response(e);
    }
    @Transactional
    public MatriculaResponse actualizarMatricula(AuthenticatedUser actor, Long id, ActualizarMatriculaRequest r) {
        validarNota(r.notaFinal());
        var e = buscar(matriculas, id, "Matrícula no encontrada.");
        if (r.estado() == EstadoMatricula.CANCELADA && e.getEstado() != EstadoMatricula.CANCELADA)
            throw regla(HttpStatus.BAD_REQUEST, "La cancelación debe tramitarse mediante una solicitud.");
        if (e.getEstado() == EstadoMatricula.CANCELADA && r.estado() != EstadoMatricula.CANCELADA)
            throw regla(HttpStatus.CONFLICT, "Una matrícula cancelada no puede reactivarse desde este CRUD.");
        var old = response(e);
        e.actualizar(r.estado(), r.notaFinal());
        audit(actor, "matricula_asignatura", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }

    @Transactional(readOnly = true)
    public List<ActividadAdminResponse> actividades(Long estudianteId) {
        List<ActividadEstudiantilEntity> list = estudianteId == null
                ? actividades.findAll() : actividades.findAllByEstudiante_IdOrderByFechaInicioDesc(estudianteId);
        return list.stream().map(this::response).toList();
    }
    @Transactional(readOnly = true)
    public ActividadAdminResponse actividad(Long id) { return response(buscar(actividades, id, "Actividad no encontrada.")); }
    @Transactional
    public ActividadAdminResponse crearActividad(AuthenticatedUser actor, ActividadRequest r) {
        validarFechas(r.fechaInicio(), r.fechaFin());
        var e = actividades.save(new ActividadEstudiantilEntity(buscar(estudiantes, r.estudianteId(),
                "Estudiante no encontrado."), r.categoria(), limpio(r.nombre()), r.descripcion(),
                r.organizacion(), r.fechaInicio(), r.fechaFin(), r.evidenciaUrl(), usuarioActor(actor)));
        audit(actor, "actividad_estudiantil", e.getId(), OperacionAuditoria.INSERT, null, response(e));
        return response(e);
    }
    @Transactional
    public ActividadAdminResponse actualizarActividad(AuthenticatedUser actor, Long id, ActividadRequest r) {
        validarFechas(r.fechaInicio(), r.fechaFin());
        var e = buscar(actividades, id, "Actividad no encontrada.");
        var old = response(e);
        if (!e.getEstudiante().getId().equals(r.estudianteId()))
            throw regla(HttpStatus.BAD_REQUEST, "No se puede reasignar una actividad a otro estudiante.");
        e.actualizar(r.categoria(), limpio(r.nombre()), r.descripcion(), r.organizacion(),
                r.fechaInicio(), r.fechaFin(), r.evidenciaUrl());
        audit(actor, "actividad_estudiantil", id, OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }
    @Transactional
    public void eliminarActividad(AuthenticatedUser actor, Long id) {
        var e = buscar(actividades, id, "Actividad no encontrada.");
        var old = response(e);
        actividades.delete(e);
        audit(actor, "actividad_estudiantil", id, OperacionAuditoria.DELETE, old, null);
    }

    @Transactional(readOnly = true)
    public PerfilAdminResponse perfil(Long estudianteId) {
        buscar(estudiantes, estudianteId, "Estudiante no encontrado.");
        return perfiles.findByEstudiante_Id(estudianteId).map(this::response).orElse(null);
    }
    @Transactional
    public PerfilAdminResponse guardarPerfil(AuthenticatedUser actor, Long estudianteId, PerfilRequest r) {
        var student = buscar(estudiantes, estudianteId, "Estudiante no encontrado.");
        var found = perfiles.findByEstudiante_Id(estudianteId);
        Object old = found.map(this::response).orElse(null);
        var e = found.orElseGet(() -> new PerfilSociodemograficoEntity(
                student, null, null, null, false, null));
        e.actualizar(r.municipioResidencia(), r.estrato(), r.situacionLaboral(),
                r.requiereApoyo(), r.observacion());
        e = perfiles.save(e);
        audit(actor, "perfil_sociodemografico", e.getId(),
                old == null ? OperacionAuditoria.INSERT : OperacionAuditoria.UPDATE, old, response(e));
        return response(e);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> auditoria(String entidad, String registroId, Long usuarioId, int page, int size) {
        if (page < 0 || size < 1 || size > 100)
            throw regla(HttpStatus.BAD_REQUEST, "El tamaño debe estar entre 1 y 100 y la página no puede ser negativa.");
        return auditorias.buscar(opcional(entidad), opcional(registroId), usuarioId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha")))
                .map(a -> new AuditoriaResponse(a.getId(), a.getUsuario() == null ? null : a.getUsuario().getId(),
                        a.getEntidad(), a.getRegistroId(), a.getOperacion(), a.getValoresAnteriores(),
                        a.getValoresNuevos(), a.getFecha()));
    }

    private <T> T crearCuenta(String email, String password, String nombre, String apellido,
            CuentaLocal<T> operation) {
        var auth = firebaseAuth.getIfAvailable();
        if (auth == null) throw regla(HttpStatus.SERVICE_UNAVAILABLE, "Firebase Authentication no está configurado.");
        try {
            var created = auth.createUser(new UserRecord.CreateRequest().setEmail(correo(email))
                    .setPassword(password).setDisplayName(limpio(nombre) + " " + limpio(apellido))
                    .setEmailVerified(false).setDisabled(false));
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    if (status == STATUS_COMMITTED) return;
                    try { auth.deleteUser(created.getUid()); }
                    catch (FirebaseAuthException e) {
                        log.error("No se pudo eliminar la cuenta Firebase tras revertir el alta local.", e);
                    }
                }
            });
            return operation.run();
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == com.google.firebase.auth.AuthErrorCode.EMAIL_ALREADY_EXISTS)
                throw regla(HttpStatus.CONFLICT, "Ya existe una cuenta Firebase con ese correo.");
            throw regla(HttpStatus.BAD_GATEWAY, "Firebase no pudo crear la cuenta.");
        }
    }

    private void syncAccount(UsuarioEntity usuario, boolean enabled, AuthenticatedUser actor) {
        if (usuario.isActivo() == enabled) return;
        if (!enabled && usuario.getRol().getNombre() == RolNombre.ADMINISTRADOR) {
            if (usuario.getId().equals(actor.userId()))
                throw regla(HttpStatus.CONFLICT, "No puedes desactivar tu propia cuenta.");
            if (usuarios.countByRol_NombreAndActivoTrue(RolNombre.ADMINISTRADOR) <= 1)
                throw regla(HttpStatus.CONFLICT, "No se puede desactivar el último administrador activo.");
        }
        var auth = firebaseAuth.getIfAvailable();
        if (auth == null) throw regla(HttpStatus.SERVICE_UNAVAILABLE, "Firebase Authentication no está configurado.");
        try {
            var firebaseUser = auth.getUserByEmail(usuario.getCorreo());
            auth.updateUser(new UserRecord.UpdateRequest(firebaseUser.getUid()).setDisabled(!enabled));
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    if (status == STATUS_COMMITTED) return;
                    try {
                        auth.updateUser(new UserRecord.UpdateRequest(firebaseUser.getUid()).setDisabled(enabled));
                    } catch (FirebaseAuthException e) {
                        log.error("No se pudo compensar el estado de Firebase al revertir PostgreSQL.", e);
                    }
                }
            });
        } catch (FirebaseAuthException e) {
            throw regla(HttpStatus.BAD_GATEWAY, "No fue posible sincronizar el estado de la cuenta Firebase.");
        }
        if (enabled) usuario.activar(); else usuario.desactivar();
    }

    private RolEntity rol(RolNombre role) {
        return roles.findByNombre(role).orElseThrow(() ->
                regla(HttpStatus.CONFLICT, "No está configurado el rol " + role + "."));
    }
    private UsuarioEntity usuarioActor(AuthenticatedUser actor) {
        return usuarios.findById(actor.userId()).orElseThrow(() ->
                regla(HttpStatus.NOT_FOUND, "Usuario autenticado no encontrado."));
    }
    private ProgramaEntity programaEntity(Long id) { return buscar(programas, id, "Programa no encontrado."); }
    private PlanEstudioEntity planEntity(Long id) { return buscar(planes, id, "Plan no encontrado."); }
    private PlanAsignaturaEntity planAsignaturaEntity(Long id) {
        return buscar(planAsignaturas, id, "Asignatura del plan no encontrada.");
    }
    private PeriodoAcademicoEntity periodoEntity(Long id) { return buscar(periodos, id, "Periodo no encontrado."); }
    private DocenteEntity docenteActivo(Long id) {
        if (id == null) return null;
        var d = buscar(docentes, id, "Docente no encontrado.");
        if (!d.isActivo()) throw regla(HttpStatus.CONFLICT, "El docente está inactivo.");
        return d;
    }

    private ProgramaResponse response(ProgramaEntity e) {
        return new ProgramaResponse(e.getId(), e.getCodigo(), e.getNombre(), e.isActivo());
    }
    private PlanResponse response(PlanEstudioEntity e) {
        return new PlanResponse(e.getId(), e.getPrograma().getId(), e.getCodigo(), e.getNombre(),
                e.getVersion(), e.getFechaInicio(), e.getFechaFin(), e.isActivo());
    }
    private AsignaturaResponse response(AsignaturaEntity e) {
        return new AsignaturaResponse(e.getId(), e.getCodigo(), e.getNombre(), e.getCreditos(), e.isActivo());
    }
    private PlanAsignaturaResponse response(PlanAsignaturaEntity e) {
        return new PlanAsignaturaResponse(e.getId(), e.getPlanEstudio().getId(), e.getAsignatura().getId(),
                e.getSemestreSugerido(), e.isObligatoria());
    }
    private PeriodoResponse response(PeriodoAcademicoEntity e) {
        return new PeriodoResponse(e.getId(), e.getCodigo(), e.getNombre(), e.getFechaInicio(), e.getFechaFin());
    }
    private OfertaResponse response(OfertaAsignaturaEntity e) {
        return new OfertaResponse(e.getId(), e.getPlanAsignatura().getId(), e.getPeriodo().getId(),
                e.getGrupo(), e.getCupo(), e.getDocente() == null ? null : e.getDocente().getId());
    }
    private UsuarioResponse usuarioResponse(UsuarioEntity e) {
        return new UsuarioResponse(e.getId(), e.getCorreo(), e.getNombre(), e.getApellido(),
                e.getRol().getNombre().name(), e.isActivo());
    }
    private EstudianteResponse response(EstudianteEntity e) {
        return new EstudianteResponse(e.getId(), e.getUsuario().getId(), e.getUsuario().getCorreo(),
                e.getUsuario().getNombre(), e.getUsuario().getApellido(), e.getPrograma().getId(),
                e.getCodigoEstudiantil(), e.getFechaIngreso(), e.getEstado(), e.getUsuario().isActivo());
    }
    private DocenteResponse response(DocenteEntity e) {
        return new DocenteResponse(e.getId(), e.getUsuario().getId(), e.getUsuario().getCorreo(),
                e.getUsuario().getNombre(), e.getUsuario().getApellido(), e.getCodigoDocente(), e.isActivo());
    }
    private MatriculaResponse response(MatriculaAsignaturaEntity e) {
        return new MatriculaResponse(e.getId(), e.getEstudiante().getId(),
                e.getOfertaAsignatura().getId(), e.getEstado(), e.getNotaFinal());
    }
    private ActividadAdminResponse response(ActividadEstudiantilEntity e) {
        return new ActividadAdminResponse(e.getId(), e.getEstudiante().getId(), e.getCategoria(), e.getNombre(),
                e.getDescripcion(), e.getOrganizacion(), e.getFechaInicio(), e.getFechaFin(),
                e.getEvidenciaUrl(), e.getCreadaEn());
    }
    private PerfilAdminResponse response(PerfilSociodemograficoEntity e) {
        return new PerfilAdminResponse(e.getEstudiante().getId(), e.getMunicipioResidencia(), e.getEstrato(),
                e.getSituacionLaboral(), e.isRequiereApoyo(), e.getObservacion(), e.getActualizadoEn());
    }
    private void audit(AuthenticatedUser actor, String table, Object id, OperacionAuditoria op,
            Object before, Object after) {
        auditorias.save(new AuditoriaCambioEntity(usuarioActor(actor), table, String.valueOf(id), op,
                before == null ? null : mapper.valueToTree(before),
                after == null ? null : mapper.valueToTree(after)));
    }
    private <T, ID> T buscar(org.springframework.data.repository.CrudRepository<T, ID> repository,
            ID id, String message) {
        return repository.findById(id).orElseThrow(() -> regla(HttpStatus.NOT_FOUND, message));
    }
    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null && fin.isBefore(inicio))
            throw regla(HttpStatus.BAD_REQUEST, "La fecha final no puede ser anterior a la inicial.");
    }
    private void validarNota(BigDecimal nota) {
        if (nota != null && (nota.compareTo(BigDecimal.ZERO) < 0
                || nota.compareTo(new BigDecimal("5.00")) > 0 || nota.scale() > 2))
            throw regla(HttpStatus.BAD_REQUEST, "La nota debe estar entre 0 y 5 con máximo dos decimales.");
    }
    private String limpio(String s) { return s.trim(); }
    private String opcional(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private String correo(String s) { return s.trim().toLowerCase(java.util.Locale.ROOT); }
    private ReglaNegocioException regla(HttpStatus status, String message) {
        return new ReglaNegocioException(status, message);
    }
    @FunctionalInterface private interface CuentaLocal<T> { T run(); }
}
