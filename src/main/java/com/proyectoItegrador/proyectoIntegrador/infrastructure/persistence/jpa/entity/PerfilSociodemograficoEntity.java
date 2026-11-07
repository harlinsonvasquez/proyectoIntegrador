package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "perfil_sociodemografico")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerfilSociodemograficoEntity extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estudiante_id", nullable = false, unique = true)
	private EstudianteEntity estudiante;

	@Column(name = "municipio_residencia", length = 100)
	private String municipioResidencia;

	@Column(name = "estrato")
	private Short estrato;

	@Column(name = "situacion_laboral", length = 50)
	private String situacionLaboral;

	@Column(name = "requiere_apoyo", nullable = false)
	private boolean requiereApoyo;

	@Column(name = "observacion", length = 1000)
	private String observacion;

	@Column(name = "actualizado_en", nullable = false)
	private OffsetDateTime actualizadoEn;

	public PerfilSociodemograficoEntity(EstudianteEntity estudiante, String municipioResidencia,
			Short estrato, String situacionLaboral, boolean requiereApoyo, String observacion) {
		this.estudiante = estudiante;
		actualizar(municipioResidencia, estrato, situacionLaboral, requiereApoyo, observacion);
	}

	public void actualizar(String municipioResidencia, Short estrato, String situacionLaboral,
			boolean requiereApoyo, String observacion) {
		this.municipioResidencia = municipioResidencia;
		this.estrato = estrato;
		this.situacionLaboral = situacionLaboral;
		this.requiereApoyo = requiereApoyo;
		this.observacion = observacion;
		this.actualizadoEn = OffsetDateTime.now();
	}

}
