package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "prerrequisito")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrerrequisitoEntity {

	@EmbeddedId
	private PrerrequisitoId id;

	@MapsId("planAsignaturaId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_asignatura_id", nullable = false)
	private PlanAsignaturaEntity planAsignatura;

	@MapsId("prerrequisitoPlanAsignaturaId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "prerrequisito_plan_asignatura_id", nullable = false)
	private PlanAsignaturaEntity prerrequisito;

	public PrerrequisitoEntity(PlanAsignaturaEntity planAsignatura, PlanAsignaturaEntity prerrequisito) {
		this.id = new PrerrequisitoId(planAsignatura.getId(), prerrequisito.getId());
		this.planAsignatura = planAsignatura;
		this.prerrequisito = prerrequisito;
	}

}
