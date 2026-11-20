package com.proyectoItegrador.proyectoIntegrador.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gestion_estudiantil", name = "evaluacion_asignatura")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluacionAsignaturaEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_asignatura_id", nullable = false)
    private OfertaAsignaturaEntity oferta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_creador_id", nullable = false)
    private DocenteEntity docenteCreador;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "fecha_limite")
    private OffsetDateTime fechaLimite;

    @Column(name = "activa", nullable = false)
    private boolean activa = true;

    @Column(name = "creada_en", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime creadaEn;

    @Column(name = "actualizada_en", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime actualizadaEn;

    public EvaluacionAsignaturaEntity(OfertaAsignaturaEntity oferta, DocenteEntity docenteCreador,
            String nombre, String descripcion, BigDecimal porcentaje, OffsetDateTime fechaLimite) {
        this.oferta = oferta;
        this.docenteCreador = docenteCreador;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.porcentaje = porcentaje;
        this.fechaLimite = fechaLimite;
    }

    public void actualizar(String nombre, String descripcion, BigDecimal porcentaje,
            OffsetDateTime fechaLimite, boolean activa) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.porcentaje = porcentaje;
        this.fechaLimite = fechaLimite;
        this.activa = activa;
    }
}
