package com.proyectoItegrador.proyectoIntegrador.infrastructure.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.CategoriaActividad;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoEstudiante;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.EstadoMatricula;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.OperacionAuditoria;
import com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity.RolNombre;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class AdminDtos {
    private AdminDtos() {}

    public record ProgramaRequest(@NotBlank @Size(max = 30) String codigo,
            @NotBlank @Size(max = 150) String nombre, boolean activo) {}
    public record PlanRequest(@NotNull Long programaId, @NotBlank @Size(max = 30) String codigo,
            @NotBlank @Size(max = 150) String nombre, @NotBlank @Size(max = 30) String version,
            LocalDate fechaInicio, LocalDate fechaFin, boolean activo) {}
    public record AsignaturaRequest(@NotBlank @Size(max = 30) String codigo,
            @NotBlank @Size(max = 150) String nombre, @NotNull @Positive Short creditos, boolean activo) {}
    public record PlanAsignaturaRequest(@NotNull Long planEstudioId, @NotNull Long asignaturaId,
            @Positive Short semestreSugerido, boolean obligatoria) {}
    public record PrerrequisitoRequest(@NotNull Long planAsignaturaId,
            @NotNull Long prerrequisitoPlanAsignaturaId) {}
    public record PeriodoRequest(@NotBlank @Size(max = 30) String codigo,
            @NotBlank @Size(max = 100) String nombre, @NotNull LocalDate fechaInicio,
            @NotNull LocalDate fechaFin) {}
    public record OfertaRequest(@NotNull Long planAsignaturaId, @NotNull Long periodoId,
            @NotBlank @Size(max = 20) String grupo, @Positive Integer cupo, Long docenteId) {}
    public record CrearEstudianteRequest(@Email @NotBlank @Size(max = 254) String correo,
            @NotBlank @Size(min = 8, max = 128) String contrasena,
            @NotBlank @Size(max = 100) String nombre, @NotBlank @Size(max = 100) String apellido,
            @NotNull Long programaId, @NotBlank @Size(max = 30) String codigoEstudiantil,
            LocalDate fechaIngreso) {}
    public record ActualizarEstudianteRequest(@NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 100) String apellido, @NotNull Long programaId,
            @NotBlank @Size(max = 30) String codigoEstudiantil, LocalDate fechaIngreso,
            @NotNull EstadoEstudiante estado) {}
    public record CrearDocenteRequest(@Email @NotBlank @Size(max = 254) String correo,
            @NotBlank @Size(min = 8, max = 128) String contrasena,
            @NotBlank @Size(max = 100) String nombre, @NotBlank @Size(max = 100) String apellido,
            @Size(max = 30) String codigoDocente) {}
    public record ActualizarDocenteRequest(@NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 100) String apellido, @Size(max = 30) String codigoDocente,
            @NotNull Boolean activo) {}
    public record MatriculaRequest(@NotNull Long estudianteId, @NotNull Long ofertaAsignaturaId,
            @NotNull EstadoMatricula estado, @PositiveOrZero BigDecimal notaFinal) {}
    public record ActualizarMatriculaRequest(@NotNull EstadoMatricula estado,
            @PositiveOrZero BigDecimal notaFinal) {}
    public record ActividadRequest(@NotNull Long estudianteId, @NotNull CategoriaActividad categoria,
            @NotBlank @Size(max = 200) String nombre, @Size(max = 2000) String descripcion,
            @Size(max = 200) String organizacion, LocalDate fechaInicio, LocalDate fechaFin,
            String evidenciaUrl) {}
    public record PerfilRequest(@Size(max = 100) String municipioResidencia,
            @Min(1) @Max(6) Short estrato, @Size(max = 50) String situacionLaboral,
            boolean requiereApoyo, @Size(max = 1000) String observacion) {}
    public record CambiarRolRequest(@NotNull RolNombre rol, @Size(max = 30) String codigoDocente) {}
    public record UsuarioResponse(Long id, String correo, String nombre, String apellido,
            String rol, boolean activo) {}
    public record EstudianteResponse(Long id, Long usuarioId, String correo, String nombre,
            String apellido, Long programaId, String codigoEstudiantil, LocalDate fechaIngreso,
            EstadoEstudiante estado, boolean usuarioActivo) {}
    public record DocenteResponse(Long id, Long usuarioId, String correo, String nombre,
            String apellido, String codigoDocente, boolean activo) {}
    public record ProgramaResponse(Long id, String codigo, String nombre, boolean activo) {}
    public record PlanResponse(Long id, Long programaId, String codigo, String nombre, String version,
            LocalDate fechaInicio, LocalDate fechaFin, boolean activo) {}
    public record AsignaturaResponse(Long id, String codigo, String nombre, Short creditos, boolean activo) {}
    public record PlanAsignaturaResponse(Long id, Long planEstudioId, Long asignaturaId,
            Short semestreSugerido, boolean obligatoria) {}
    public record PrerrequisitoResponse(Long planAsignaturaId, Long prerrequisitoPlanAsignaturaId) {}
    public record PeriodoResponse(Long id, String codigo, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {}
    public record OfertaResponse(Long id, Long planAsignaturaId, Long periodoId, String grupo,
            Integer cupo, Long docenteId) {}
    public record MatriculaResponse(Long id, Long estudianteId, Long ofertaAsignaturaId,
            EstadoMatricula estado, BigDecimal notaFinal) {}
    public record ActividadAdminResponse(Long id, Long estudianteId, CategoriaActividad categoria,
            String nombre, String descripcion, String organizacion, LocalDate fechaInicio,
            LocalDate fechaFin, String evidenciaUrl, OffsetDateTime creadaEn) {}
    public record PerfilAdminResponse(Long estudianteId, String municipioResidencia, Short estrato,
            String situacionLaboral, boolean requiereApoyo, String observacion, OffsetDateTime actualizadoEn) {}
    public record AuditoriaResponse(Long id, Long usuarioId, String entidad, String registroId,
            OperacionAuditoria operacion, JsonNode valoresAnteriores, JsonNode valoresNuevos,
            OffsetDateTime fecha) {}
}
