package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "programa")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProgramaEntity extends BaseEntity {

	@Column(name = "codigo", nullable = false, unique = true, length = 30)
	private String codigo;

	@Column(name = "nombre", nullable = false, length = 150)
	private String nombre;

	@Column(name = "activo", nullable = false)
	private boolean activo;

	public ProgramaEntity(String codigo, String nombre, boolean activo) {
		this.codigo = codigo;
		this.nombre = nombre;
		this.activo = activo;
	}

	public void actualizar(String codigo, String nombre, boolean activo) {
		this.codigo = codigo;
		this.nombre = nombre;
		this.activo = activo;
	}

	public void desactivar() {
		this.activo = false;
	}

}
