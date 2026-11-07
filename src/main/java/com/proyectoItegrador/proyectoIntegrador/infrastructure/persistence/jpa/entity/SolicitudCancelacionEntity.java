package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "solicitud_cancelacion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SolicitudCancelacionEntity extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estudiante_id", nullable = false, insertable = false, updatable = false)
	private EstudianteEntity estudiante;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumns({
			@JoinColumn(name = "matricula_id", referencedColumnName = "id", nullable = false),
			@JoinColumn(name = "estudiante_id", referencedColumnName = "estudiante_id", nullable = false)
	})
	private MatriculaAsignaturaEntity matricula;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "asignada_docente_id")
	private DocenteEntity asignadaDocente;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoSolicitud estado;

	@Column(name = "motivo", nullable = false, length = 2000)
	private String motivo;

	@Column(name = "fecha_creacion", nullable = false)
	private OffsetDateTime fechaCreacion;

	@Column(name = "fecha_cierre")
	private OffsetDateTime fechaCierre;

	public SolicitudCancelacionEntity(EstudianteEntity estudiante, MatriculaAsignaturaEntity matricula,
			String motivo) {
		this.estudiante = estudiante;
		this.matricula = matricula;
		this.motivo = motivo;
		this.estado = EstadoSolicitud.PENDIENTE;
		this.fechaCreacion = OffsetDateTime.now();
	}

	public void actualizarEstado(EstadoSolicitud estado, DocenteEntity docente) {
		this.estado = estado;
		if (docente != null) this.asignadaDocente = docente;
		this.fechaCierre = (estado == EstadoSolicitud.APROBADA || estado == EstadoSolicitud.RECHAZADA)
				? OffsetDateTime.now() : null;
	}

}
