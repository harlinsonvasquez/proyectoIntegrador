package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "estudiante")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstudianteEntity extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false, unique = true)
	private UsuarioEntity usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "programa_id", nullable = false)
	private ProgramaEntity programa;

	@Column(name = "codigo_estudiantil", nullable = false, unique = true, length = 30)
	private String codigoEstudiantil;

	@Column(name = "fecha_ingreso")
	private LocalDate fechaIngreso;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoEstudiante estado;

	public EstudianteEntity(UsuarioEntity usuario, ProgramaEntity programa, String codigoEstudiantil,
			LocalDate fechaIngreso, EstadoEstudiante estado) {
		this.usuario = usuario;
		this.programa = programa;
		this.codigoEstudiantil = codigoEstudiantil;
		this.fechaIngreso = fechaIngreso;
		this.estado = estado;
	}

	public void actualizar(ProgramaEntity programa, String codigoEstudiantil,
			LocalDate fechaIngreso, EstadoEstudiante estado) {
		this.programa = programa;
		this.codigoEstudiantil = codigoEstudiantil;
		this.fechaIngreso = fechaIngreso;
		this.estado = estado;
	}

	public void inactivar() {
		this.estado = EstadoEstudiante.INACTIVO;
		this.usuario.desactivar();
	}

	public void reactivar() {
		this.estado = EstadoEstudiante.ACTIVO;
		this.usuario.activar();
	}

}
