package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "actividad_estudiantil")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActividadEstudiantilEntity extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estudiante_id", nullable = false)
	private EstudianteEntity estudiante;

	@Enumerated(EnumType.STRING)
	@Column(name = "categoria", nullable = false, length = 30)
	private CategoriaActividad categoria;

	@Column(name = "nombre", nullable = false, length = 200)
	private String nombre;

	@Column(name = "descripcion", length = 2000)
	private String descripcion;

	@Column(name = "organizacion", length = 200)
	private String organizacion;

	@Column(name = "fecha_inicio")
	private LocalDate fechaInicio;

	@Column(name = "fecha_fin")
	private LocalDate fechaFin;

	@Column(name = "evidencia_url", columnDefinition = "text")
	private String evidenciaUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "registrada_por_usuario_id")
	private UsuarioEntity registradaPor;

	@Column(name = "creada_en", nullable = false)
	private OffsetDateTime creadaEn;

	public ActividadEstudiantilEntity(EstudianteEntity estudiante, CategoriaActividad categoria,
			String nombre, String descripcion, String organizacion, LocalDate fechaInicio,
			LocalDate fechaFin, String evidenciaUrl, UsuarioEntity registradaPor) {
		this.estudiante = estudiante;
		this.categoria = categoria;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.organizacion = organizacion;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.evidenciaUrl = evidenciaUrl;
		this.registradaPor = registradaPor;
		this.creadaEn = OffsetDateTime.now();
	}

}
