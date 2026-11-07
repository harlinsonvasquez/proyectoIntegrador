package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "usuario")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsuarioEntity extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "rol_id", nullable = false)
	private RolEntity rol;

	@Column(name = "correo", nullable = false, length = 254)
	private String correo;

	@Column(name = "nombre", nullable = false, length = 100)
	private String nombre;

	@Column(name = "apellido", nullable = false, length = 100)
	private String apellido;

	@Column(name = "activo", nullable = false)
	private boolean activo;

	@Column(name = "creado_en", nullable = false)
	private OffsetDateTime creadoEn;

	@Column(name = "actualizado_en", nullable = false)
	private OffsetDateTime actualizadoEn;

	public void actualizarDatosPersonales(String nombre, String apellido) {
		this.nombre = nombre;
		this.apellido = apellido;
		this.actualizadoEn = OffsetDateTime.now();
	}

}
