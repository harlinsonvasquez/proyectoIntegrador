package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "periodo_academico")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PeriodoAcademicoEntity extends BaseEntity {

	@Column(name = "codigo", nullable = false, unique = true, length = 30)
	private String codigo;

	@Column(name = "nombre", nullable = false, length = 100)
	private String nombre;

	@Column(name = "fecha_inicio", nullable = false)
	private LocalDate fechaInicio;

	@Column(name = "fecha_fin", nullable = false)
	private LocalDate fechaFin;

}
