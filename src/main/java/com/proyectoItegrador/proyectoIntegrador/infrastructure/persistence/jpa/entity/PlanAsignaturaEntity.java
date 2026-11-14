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
@Table(schema = "gestion_estudiantil", name = "plan_asignatura")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanAsignaturaEntity extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_estudio_id", nullable = false)
	private PlanEstudioEntity planEstudio;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "asignatura_id", nullable = false)
	private AsignaturaEntity asignatura;

	@Column(name = "semestre_sugerido")
	private Short semestreSugerido;

	@Column(name = "obligatoria", nullable = false)
	private boolean obligatoria;

	public PlanAsignaturaEntity(PlanEstudioEntity planEstudio, AsignaturaEntity asignatura,
			Short semestreSugerido, boolean obligatoria) {
		this.planEstudio = planEstudio;
		this.asignatura = asignatura;
		this.semestreSugerido = semestreSugerido;
		this.obligatoria = obligatoria;
	}

	public void actualizar(PlanEstudioEntity planEstudio, AsignaturaEntity asignatura,
			Short semestreSugerido, boolean obligatoria) {
		this.planEstudio = planEstudio;
		this.asignatura = asignatura;
		this.semestreSugerido = semestreSugerido;
		this.obligatoria = obligatoria;
	}

}
