-- Esquema inicial para el sistema de gestión de solicitudes estudiantiles.
-- IF NOT EXISTS permite adoptar una base local donde estos objetos ya fueron
-- creados manualmente. Esta migración no sustituye una comparación del esquema:
-- Hibernate ddl-auto=validate debe confirmar que las tablas existentes coinciden.

CREATE SCHEMA IF NOT EXISTS gestion_estudiantil;

CREATE TABLE IF NOT EXISTS gestion_estudiantil.rol (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(30) NOT NULL UNIQUE,
    descripcion     VARCHAR(200),
    CONSTRAINT ck_rol_nombre
        CHECK (nombre IN ('ESTUDIANTE', 'DOCENTE', 'ADMINISTRADOR'))
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.usuario (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rol_id          BIGINT NOT NULL REFERENCES gestion_estudiantil.rol(id),
    correo          VARCHAR(254) NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    apellido        VARCHAR(100) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_usuario_correo_ci
    ON gestion_estudiantil.usuario (LOWER(correo));

CREATE TABLE IF NOT EXISTS gestion_estudiantil.programa (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo          VARCHAR(30) NOT NULL UNIQUE,
    nombre          VARCHAR(150) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.estudiante (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id          BIGINT NOT NULL UNIQUE REFERENCES gestion_estudiantil.usuario(id),
    programa_id         BIGINT NOT NULL REFERENCES gestion_estudiantil.programa(id),
    codigo_estudiantil  VARCHAR(30) NOT NULL UNIQUE,
    fecha_ingreso       DATE,
    estado              VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT ck_estudiante_estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO', 'GRADUADO', 'RETIRADO'))
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.docente (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id      BIGINT NOT NULL UNIQUE REFERENCES gestion_estudiantil.usuario(id),
    codigo_docente  VARCHAR(30) UNIQUE,
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.plan_estudio (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    programa_id     BIGINT NOT NULL REFERENCES gestion_estudiantil.programa(id),
    codigo          VARCHAR(30) NOT NULL,
    nombre          VARCHAR(150) NOT NULL,
    version         VARCHAR(30) NOT NULL,
    fecha_inicio    DATE,
    fecha_fin       DATE,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_plan_codigo_version UNIQUE (programa_id, codigo, version),
    CONSTRAINT ck_plan_fechas
        CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.asignatura (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo          VARCHAR(30) NOT NULL UNIQUE,
    nombre          VARCHAR(150) NOT NULL,
    creditos        SMALLINT NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_asignatura_creditos CHECK (creditos > 0)
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.plan_asignatura (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_estudio_id     BIGINT NOT NULL REFERENCES gestion_estudiantil.plan_estudio(id),
    asignatura_id       BIGINT NOT NULL REFERENCES gestion_estudiantil.asignatura(id),
    semestre_sugerido   SMALLINT,
    obligatoria         BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_plan_asignatura UNIQUE (plan_estudio_id, asignatura_id),
    CONSTRAINT ck_plan_asignatura_semestre
        CHECK (semestre_sugerido IS NULL OR semestre_sugerido > 0)
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.prerrequisito (
    plan_asignatura_id BIGINT NOT NULL
        REFERENCES gestion_estudiantil.plan_asignatura(id),
    prerrequisito_plan_asignatura_id BIGINT NOT NULL
        REFERENCES gestion_estudiantil.plan_asignatura(id),
    PRIMARY KEY (plan_asignatura_id, prerrequisito_plan_asignatura_id),
    CONSTRAINT ck_prerrequisito_no_propio
        CHECK (plan_asignatura_id <> prerrequisito_plan_asignatura_id)
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.periodo_academico (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo          VARCHAR(30) NOT NULL UNIQUE,
    nombre          VARCHAR(100) NOT NULL,
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE NOT NULL,
    CONSTRAINT ck_periodo_fechas CHECK (fecha_fin >= fecha_inicio)
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.oferta_asignatura (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_asignatura_id  BIGINT NOT NULL
        REFERENCES gestion_estudiantil.plan_asignatura(id),
    periodo_id          BIGINT NOT NULL
        REFERENCES gestion_estudiantil.periodo_academico(id),
    grupo               VARCHAR(20) NOT NULL DEFAULT '1',
    cupo                INTEGER,
    docente_id          BIGINT REFERENCES gestion_estudiantil.docente(id),
    CONSTRAINT uq_oferta UNIQUE (plan_asignatura_id, periodo_id, grupo),
    CONSTRAINT ck_oferta_cupo CHECK (cupo IS NULL OR cupo > 0)
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.matricula_asignatura (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    estudiante_id           BIGINT NOT NULL
        REFERENCES gestion_estudiantil.estudiante(id),
    oferta_asignatura_id    BIGINT NOT NULL
        REFERENCES gestion_estudiantil.oferta_asignatura(id),
    estado                  VARCHAR(20) NOT NULL DEFAULT 'MATRICULADA',
    nota_final              NUMERIC(4,2),
    fecha_estado            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_matricula_estudiante_oferta
        UNIQUE (estudiante_id, oferta_asignatura_id),
    CONSTRAINT uq_matricula_id_estudiante UNIQUE (id, estudiante_id),
    CONSTRAINT ck_matricula_estado
        CHECK (estado IN (
            'MATRICULADA', 'CURSANDO', 'APROBADA',
            'REPROBADA', 'CANCELADA', 'PENDIENTE'
        )),
    CONSTRAINT ck_matricula_nota
        CHECK (nota_final IS NULL OR (nota_final >= 0 AND nota_final <= 5))
);

CREATE INDEX IF NOT EXISTS ix_matricula_estudiante
    ON gestion_estudiantil.matricula_asignatura (estudiante_id);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.solicitud_cancelacion (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    estudiante_id       BIGINT NOT NULL,
    matricula_id        BIGINT NOT NULL,
    asignada_docente_id BIGINT
        REFERENCES gestion_estudiantil.docente(id),
    estado              VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    motivo              VARCHAR(2000) NOT NULL,
    fecha_creacion      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre        TIMESTAMPTZ,
    CONSTRAINT fk_solicitud_matricula_estudiante
        FOREIGN KEY (matricula_id, estudiante_id)
        REFERENCES gestion_estudiantil.matricula_asignatura (id, estudiante_id),
    CONSTRAINT ck_solicitud_estado
        CHECK (estado IN ('PENDIENTE', 'EN_REVISION', 'APROBADA', 'RECHAZADA')),
    CONSTRAINT ck_solicitud_cierre
        CHECK (fecha_cierre IS NULL OR fecha_cierre >= fecha_creacion)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_solicitud_activa_por_matricula
    ON gestion_estudiantil.solicitud_cancelacion (matricula_id)
    WHERE estado IN ('PENDIENTE', 'EN_REVISION');

CREATE INDEX IF NOT EXISTS ix_solicitud_estado_fecha
    ON gestion_estudiantil.solicitud_cancelacion (estado, fecha_creacion);

CREATE INDEX IF NOT EXISTS ix_solicitud_estudiante_fecha
    ON gestion_estudiantil.solicitud_cancelacion (estudiante_id, fecha_creacion DESC);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.seguimiento_solicitud (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitud_id            BIGINT NOT NULL
        REFERENCES gestion_estudiantil.solicitud_cancelacion(id),
    responsable_usuario_id  BIGINT NOT NULL
        REFERENCES gestion_estudiantil.usuario(id),
    estado                  VARCHAR(20) NOT NULL,
    observacion             VARCHAR(2000),
    fecha                   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_seguimiento_estado
        CHECK (estado IN ('PENDIENTE', 'EN_REVISION', 'APROBADA', 'RECHAZADA'))
);

CREATE INDEX IF NOT EXISTS ix_seguimiento_solicitud_fecha
    ON gestion_estudiantil.seguimiento_solicitud (solicitud_id, fecha);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.perfil_sociodemografico (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    estudiante_id           BIGINT NOT NULL UNIQUE
        REFERENCES gestion_estudiantil.estudiante(id),
    municipio_residencia    VARCHAR(100),
    estrato                 SMALLINT,
    situacion_laboral       VARCHAR(50),
    requiere_apoyo          BOOLEAN NOT NULL DEFAULT FALSE,
    observacion             VARCHAR(1000),
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_perfil_estrato
        CHECK (estrato IS NULL OR estrato BETWEEN 1 AND 6)
);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.actividad_estudiantil (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    estudiante_id           BIGINT NOT NULL
        REFERENCES gestion_estudiantil.estudiante(id),
    categoria               VARCHAR(30) NOT NULL,
    nombre                  VARCHAR(200) NOT NULL,
    descripcion             VARCHAR(2000),
    organizacion            VARCHAR(200),
    fecha_inicio            DATE,
    fecha_fin               DATE,
    evidencia_url           TEXT,
    registrada_por_usuario_id BIGINT
        REFERENCES gestion_estudiantil.usuario(id),
    creada_en               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_actividad_categoria
        CHECK (categoria IN (
            'CONGRESO', 'SEMILLERO', 'PROYECTO_INVESTIGACION',
            'PUBLICACION', 'DEPORTE', 'OTRA'
        )),
    CONSTRAINT ck_actividad_fechas
        CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX IF NOT EXISTS ix_actividad_estudiante_categoria
    ON gestion_estudiantil.actividad_estudiantil (estudiante_id, categoria);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.auditoria_cambio (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id          BIGINT REFERENCES gestion_estudiantil.usuario(id),
    entidad             VARCHAR(100) NOT NULL,
    registro_id         VARCHAR(100) NOT NULL,
    operacion           VARCHAR(10) NOT NULL,
    valores_anteriores  JSONB,
    valores_nuevos      JSONB,
    fecha               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_auditoria_operacion
        CHECK (operacion IN ('INSERT', 'UPDATE', 'DELETE'))
);

CREATE INDEX IF NOT EXISTS ix_auditoria_entidad_registro_fecha
    ON gestion_estudiantil.auditoria_cambio (entidad, registro_id, fecha DESC);

CREATE INDEX IF NOT EXISTS ix_auditoria_usuario_fecha
    ON gestion_estudiantil.auditoria_cambio (usuario_id, fecha DESC);

CREATE TABLE IF NOT EXISTS gestion_estudiantil.outbox_evento (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo                VARCHAR(100) NOT NULL,
    agregado_tipo       VARCHAR(100) NOT NULL,
    agregado_id         VARCHAR(100) NOT NULL,
    usuario_destino_id  BIGINT REFERENCES gestion_estudiantil.usuario(id),
    payload             JSONB NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    procesado_en        TIMESTAMPTZ,
    intentos            INTEGER NOT NULL DEFAULT 0,
    ultimo_error        TEXT,
    CONSTRAINT ck_outbox_intentos CHECK (intentos >= 0)
);

CREATE INDEX IF NOT EXISTS ix_outbox_pendiente
    ON gestion_estudiantil.outbox_evento (creado_en)
    WHERE procesado_en IS NULL;

INSERT INTO gestion_estudiantil.rol (nombre, descripcion) VALUES
    ('ESTUDIANTE', 'Usuario estudiante'),
    ('DOCENTE', 'Docente autorizado'),
    ('ADMINISTRADOR', 'Administrador del sistema')
ON CONFLICT (nombre) DO NOTHING;
