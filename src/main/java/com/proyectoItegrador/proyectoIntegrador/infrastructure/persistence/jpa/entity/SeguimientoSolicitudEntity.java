package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "seguimiento_solicitud")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeguimientoSolicitudEntity extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "solicitud_id", nullable = false)
	private SolicitudCancelacionEntity solicitud;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "responsable_usuario_id", nullable = false)
	private UsuarioEntity responsable;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoSolicitud estado;

	@Column(name = "observacion", length = 2000)
	private String observacion;

	@Column(name = "fecha", nullable = false)
	private OffsetDateTime fecha;

	public SeguimientoSolicitudEntity(SolicitudCancelacionEntity solicitud, UsuarioEntity responsable,
			EstadoSolicitud estado, String observacion) {
		this.solicitud = solicitud;
		this.responsable = responsable;
		this.estado = estado;
		this.observacion = observacion;
		this.fecha = OffsetDateTime.now();
	}

}
