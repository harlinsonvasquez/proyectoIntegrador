package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PrerrequisitoId implements Serializable {

	@Column(name = "plan_asignatura_id")
	private Long planAsignaturaId;

	@Column(name = "prerrequisito_plan_asignatura_id")
	private Long prerrequisitoPlanAsignaturaId;

	protected PrerrequisitoId() {
	}

	public PrerrequisitoId(Long planAsignaturaId, Long prerrequisitoPlanAsignaturaId) {
		this.planAsignaturaId = planAsignaturaId;
		this.prerrequisitoPlanAsignaturaId = prerrequisitoPlanAsignaturaId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PrerrequisitoId that)) {
			return false;
		}
		return Objects.equals(planAsignaturaId, that.planAsignaturaId)
				&& Objects.equals(prerrequisitoPlanAsignaturaId, that.prerrequisitoPlanAsignaturaId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(planAsignaturaId, prerrequisitoPlanAsignaturaId);
	}
}
