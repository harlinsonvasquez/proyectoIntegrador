package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(schema = "gestion_estudiantil", name = "auditoria_cambio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditoriaCambioEntity extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	private UsuarioEntity usuario;

	@Column(name = "entidad", nullable = false, length = 100)
	private String entidad;

	@Column(name = "registro_id", nullable = false, length = 100)
	private String registroId;

	@Enumerated(EnumType.STRING)
	@Column(name = "operacion", nullable = false, length = 10)
	private OperacionAuditoria operacion;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "valores_anteriores", columnDefinition = "jsonb")
	private JsonNode valoresAnteriores;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "valores_nuevos", columnDefinition = "jsonb")
	private JsonNode valoresNuevos;

	@Column(name = "fecha", nullable = false)
	private OffsetDateTime fecha;

	public AuditoriaCambioEntity(UsuarioEntity usuario, String entidad, String registroId,
			OperacionAuditoria operacion, JsonNode valoresAnteriores, JsonNode valoresNuevos) {
		this.usuario = usuario;
		this.entidad = entidad;
		this.registroId = registroId;
		this.operacion = operacion;
		this.valoresAnteriores = valoresAnteriores;
		this.valoresNuevos = valoresNuevos;
		this.fecha = OffsetDateTime.now();
	}

}
