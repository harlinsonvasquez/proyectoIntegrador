package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "docente")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocenteEntity extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false, unique = true)
	private UsuarioEntity usuario;

	@Column(name = "codigo_docente", unique = true, length = 30)
	private String codigoDocente;

	@Column(name = "activo", nullable = false)
	private boolean activo;

	public DocenteEntity(UsuarioEntity usuario, String codigoDocente) {
		this.usuario = usuario;
		this.codigoDocente = codigoDocente;
		this.activo = true;
	}

	public void actualizar(String codigoDocente, boolean activo) {
		this.codigoDocente = codigoDocente;
		this.activo = activo;
	}

	public void inactivar() {
		this.activo = false;
		this.usuario.desactivar();
	}

	public void reactivar() {
		this.activo = true;
		this.usuario.activar();
	}

}
