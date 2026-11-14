package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "matricula_asignatura")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatriculaAsignaturaEntity extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estudiante_id", nullable = false)
	private EstudianteEntity estudiante;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "oferta_asignatura_id", nullable = false)
	private OfertaAsignaturaEntity ofertaAsignatura;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoMatricula estado;

	@Column(name = "nota_final", precision = 4, scale = 2)
	private BigDecimal notaFinal;

	@Column(name = "fecha_estado", nullable = false)
	private OffsetDateTime fechaEstado;

	public void cancelar() {
		this.estado = EstadoMatricula.CANCELADA;
		this.fechaEstado = OffsetDateTime.now();
	}

	public MatriculaAsignaturaEntity(EstudianteEntity estudiante, OfertaAsignaturaEntity ofertaAsignatura,
			EstadoMatricula estado, BigDecimal notaFinal) {
		this.estudiante = estudiante;
		this.ofertaAsignatura = ofertaAsignatura;
		this.estado = estado;
		this.notaFinal = notaFinal;
		this.fechaEstado = OffsetDateTime.now();
	}

	public void actualizar(EstadoMatricula estado, BigDecimal notaFinal) {
		this.estado = estado;
		this.notaFinal = notaFinal;
		this.fechaEstado = OffsetDateTime.now();
	}

}
