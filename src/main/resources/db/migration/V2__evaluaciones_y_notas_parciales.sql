-- V2: evaluaciones por oferta y notas parciales por matrícula.
-- No actualiza ni elimina datos existentes de V1. nota_final permanece intacta.

-- Claves compuestas necesarias para garantizar que una nota parcial vincule
-- una evaluación y una matrícula de la misma oferta académica.
ALTER TABLE gestion_estudiantil.matricula_asignatura
    ADD CONSTRAINT uq_matricula_id_oferta UNIQUE (id, oferta_asignatura_id);

CREATE TABLE gestion_estudiantil.evaluacion_asignatura (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    oferta_asignatura_id    BIGINT NOT NULL
        REFERENCES gestion_estudiantil.oferta_asignatura(id),
    docente_creador_id      BIGINT NOT NULL
        REFERENCES gestion_estudiantil.docente(id),
    nombre                  VARCHAR(150) NOT NULL,
    descripcion             VARCHAR(2000),
    porcentaje              NUMERIC(5,2) NOT NULL,
    fecha_limite            TIMESTAMPTZ,
    activa                  BOOLEAN NOT NULL DEFAULT TRUE,
    creada_en               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizada_en          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_evaluacion_nombre CHECK (LENGTH(BTRIM(nombre)) > 0),
    CONSTRAINT ck_evaluacion_porcentaje CHECK (porcentaje > 0 AND porcentaje <= 100),
    CONSTRAINT uq_evaluacion_id_oferta UNIQUE (id, oferta_asignatura_id)
);

CREATE INDEX ix_evaluacion_oferta_activa
    ON gestion_estudiantil.evaluacion_asignatura (oferta_asignatura_id, activa, creada_en);

CREATE TABLE gestion_estudiantil.nota_parcial (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    evaluacion_id           BIGINT NOT NULL,
    oferta_asignatura_id    BIGINT NOT NULL,
    matricula_id            BIGINT NOT NULL,
    nota                    NUMERIC(4,2) NOT NULL,
    observacion             VARCHAR(1000),
    registrada_por_docente_id BIGINT NOT NULL
        REFERENCES gestion_estudiantil.docente(id),
    creada_en               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizada_en          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_nota_parcial_rango CHECK (nota >= 0 AND nota <= 5),
    CONSTRAINT uq_nota_parcial_evaluacion_matricula UNIQUE (evaluacion_id, matricula_id),
    CONSTRAINT fk_nota_parcial_evaluacion_oferta
        FOREIGN KEY (evaluacion_id, oferta_asignatura_id)
        REFERENCES gestion_estudiantil.evaluacion_asignatura (id, oferta_asignatura_id),
    CONSTRAINT fk_nota_parcial_matricula_oferta
        FOREIGN KEY (matricula_id, oferta_asignatura_id)
        REFERENCES gestion_estudiantil.matricula_asignatura (id, oferta_asignatura_id)
);

CREATE INDEX ix_nota_parcial_matricula
    ON gestion_estudiantil.nota_parcial (matricula_id, actualizada_en DESC);
CREATE INDEX ix_nota_parcial_evaluacion
    ON gestion_estudiantil.nota_parcial (evaluacion_id);

-- Serializa cambios de porcentajes por oferta y protege la regla incluso si
-- se insertan evaluaciones desde más de una instancia de la aplicación.
CREATE FUNCTION gestion_estudiantil.validar_porcentaje_evaluacion()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    porcentaje_actual NUMERIC(7,2);
BEGIN
    PERFORM 1
      FROM gestion_estudiantil.oferta_asignatura
     WHERE id = NEW.oferta_asignatura_id
     FOR UPDATE;

    SELECT COALESCE(SUM(porcentaje), 0)
      INTO porcentaje_actual
      FROM gestion_estudiantil.evaluacion_asignatura
     WHERE oferta_asignatura_id = NEW.oferta_asignatura_id
       AND (TG_OP = 'INSERT' OR id <> NEW.id);

    IF porcentaje_actual + NEW.porcentaje > 100 THEN
        RAISE EXCEPTION 'La suma de porcentajes de las evaluaciones no puede superar 100%%'
            USING ERRCODE = '23514', CONSTRAINT = 'ck_evaluacion_porcentaje_acumulado';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_porcentaje_evaluacion
    BEFORE INSERT OR UPDATE OF porcentaje, oferta_asignatura_id
    ON gestion_estudiantil.evaluacion_asignatura
    FOR EACH ROW
    EXECUTE FUNCTION gestion_estudiantil.validar_porcentaje_evaluacion();


