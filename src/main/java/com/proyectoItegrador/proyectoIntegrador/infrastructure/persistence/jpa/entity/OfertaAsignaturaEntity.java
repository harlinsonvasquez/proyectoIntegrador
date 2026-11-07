package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "oferta_asignatura")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfertaAsignaturaEntity extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_asignatura_id", nullable = false)
	private PlanAsignaturaEntity planAsignatura;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "periodo_id", nullable = false)
	private PeriodoAcademicoEntity periodo;

	@Column(name = "grupo", nullable = false, length = 20)
	private String grupo;

	@Column(name = "cupo")
	private Integer cupo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "docente_id")
	private DocenteEntity docente;

}
