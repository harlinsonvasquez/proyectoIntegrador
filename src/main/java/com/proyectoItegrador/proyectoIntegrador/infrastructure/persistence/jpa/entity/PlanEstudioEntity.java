package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "plan_estudio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanEstudioEntity extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "programa_id", nullable = false)
	private ProgramaEntity programa;

	@Column(name = "codigo", nullable = false, length = 30)
	private String codigo;

	@Column(name = "nombre", nullable = false, length = 150)
	private String nombre;

	@Column(name = "version", nullable = false, length = 30)
	private String version;

	@Column(name = "fecha_inicio")
	private LocalDate fechaInicio;

	@Column(name = "fecha_fin")
	private LocalDate fechaFin;

	@Column(name = "activo", nullable = false)
	private boolean activo;

}
