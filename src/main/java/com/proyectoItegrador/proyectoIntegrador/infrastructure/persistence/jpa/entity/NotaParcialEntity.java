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
@Table(schema = "gestion_estudiantil", name = "nota_parcial")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotaParcialEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private EvaluacionAsignaturaEntity evaluacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matricula_id", nullable = false)
    private MatriculaAsignaturaEntity matricula;

    @Column(name = "oferta_asignatura_id", nullable = false)
    private Long ofertaAsignaturaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrada_por_docente_id", nullable = false)
    private DocenteEntity registradaPor;

    @Column(name = "nota", nullable = false, precision = 4, scale = 2)
    private BigDecimal nota;

    @Column(name = "observacion", length = 1000)
    private String observacion;

    @Column(name = "creada_en", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime creadaEn;

    @Column(name = "actualizada_en", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime actualizadaEn;

    public NotaParcialEntity(EvaluacionAsignaturaEntity evaluacion, MatriculaAsignaturaEntity matricula,
            DocenteEntity registradaPor, BigDecimal nota, String observacion) {
        this.evaluacion = evaluacion;
        this.matricula = matricula;
        this.ofertaAsignaturaId = evaluacion.getOferta().getId();
        this.registradaPor = registradaPor;
        this.nota = nota;
        this.observacion = observacion;
    }

    public void actualizar(DocenteEntity docente, BigDecimal nota, String observacion) {
        this.registradaPor = docente;
        this.nota = nota;
        this.observacion = observacion;
    }
}
