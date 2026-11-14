package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "rol")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolEntity extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "nombre", nullable = false, unique = true, length = 30)
	private RolNombre nombre;

	@Column(name = "descripcion", length = 200)
	private String descripcion;

}
