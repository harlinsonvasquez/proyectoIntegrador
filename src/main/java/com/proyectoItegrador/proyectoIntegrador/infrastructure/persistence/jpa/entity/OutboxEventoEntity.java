package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(schema = "gestion_estudiantil", name = "outbox_evento")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventoEntity extends BaseEntity {

	@Column(name = "tipo", nullable = false, length = 100)
	private String tipo;

	@Column(name = "agregado_tipo", nullable = false, length = 100)
	private String agregadoTipo;

	@Column(name = "agregado_id", nullable = false, length = 100)
	private String agregadoId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_destino_id")
	private UsuarioEntity usuarioDestino;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "payload", nullable = false, columnDefinition = "jsonb")
	private JsonNode payload;

	@Column(name = "creado_en", nullable = false)
	private OffsetDateTime creadoEn;

	@Column(name = "procesado_en")
	private OffsetDateTime procesadoEn;

	@Column(name = "intentos", nullable = false)
	private Integer intentos;

	@Column(name = "ultimo_error", columnDefinition = "text")
	private String ultimoError;

	public OutboxEventoEntity(String tipo, String agregadoTipo, String agregadoId,
			UsuarioEntity usuarioDestino, JsonNode payload) {
		this.tipo = tipo;
		this.agregadoTipo = agregadoTipo;
		this.agregadoId = agregadoId;
		this.usuarioDestino = usuarioDestino;
		this.payload = payload;
		this.creadoEn = OffsetDateTime.now();
		this.intentos = 0;
	}

	public void marcarProcesado() {
		this.procesadoEn = OffsetDateTime.now();
		this.ultimoError = null;
	}

	public void registrarError(String error) {
		this.intentos++;
		this.ultimoError = error;
	}

}
