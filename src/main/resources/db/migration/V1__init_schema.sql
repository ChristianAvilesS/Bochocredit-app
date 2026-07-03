-- ─────────────────────────────────────────────
-- BochoCredit — Esquema PostgreSQL
-- Migrado desde modelo SQLite (app_v2.py)
-- ─────────────────────────────────────────────

CREATE TABLE roles (
    id              BIGSERIAL PRIMARY KEY,
    nombre_rol      VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(150),
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    id_rol          BIGINT REFERENCES roles(id)
);

CREATE TABLE clientes (
    id              BIGSERIAL PRIMARY KEY,
    nombres         VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    dni             VARCHAR(15)  NOT NULL UNIQUE,
    telefono        VARCHAR(20)  NOT NULL,
    email           VARCHAR(150) NOT NULL,
    direccion       VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE vehiculos (
    id              BIGSERIAL PRIMARY KEY,
    marca           VARCHAR(80)  NOT NULL,
    modelo          VARCHAR(80)  NOT NULL,
    anio            INTEGER      NOT NULL,
    precio          NUMERIC(14,2) NOT NULL,
    descripcion     VARCHAR(255),
    disponibilidad  VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE'
);

CREATE TABLE bancos (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    razon_social    VARCHAR(150) NOT NULL,
    ruc             VARCHAR(15)  NOT NULL,
    direccion       VARCHAR(255) NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE tasas_interes (
    id                   BIGSERIAL PRIMARY KEY,
    tipo_tasa            VARCHAR(30)   NOT NULL,
    tasa_interes         NUMERIC(8,4)  NOT NULL DEFAULT 0.1,
    dias_capitalizacion  INTEGER       NOT NULL DEFAULT 1,
    dias_tasa            INTEGER       NOT NULL DEFAULT 360
);

CREATE TABLE simulaciones (
    id                      BIGSERIAL PRIMARY KEY,

    tcea                    NUMERIC(10,4) NOT NULL,
    van                     NUMERIC(16,4) NOT NULL,
    tir                     NUMERIC(10,6) NOT NULL,
    saldo_financiado        NUMERIC(14,2) NOT NULL,

    plazo_meses             INTEGER       NOT NULL,
    cantidad_cuotas         INTEGER       NOT NULL,
    porc_cuota_inicial      NUMERIC(6,2)  NOT NULL,
    tipo_periodo_gracia     VARCHAR(20)   NOT NULL,
    periodo_gracia_meses    NUMERIC(6,2)  NOT NULL DEFAULT 0,
    tipo_moneda             VARCHAR(10)   NOT NULL,
    es_elegido              BOOLEAN       NOT NULL DEFAULT FALSE,
    creado_en               TIMESTAMP     NOT NULL DEFAULT now(),

    id_usuario              BIGINT NOT NULL REFERENCES usuarios(id),
    id_cliente              BIGINT NOT NULL REFERENCES clientes(id),
    id_vehiculo             BIGINT NOT NULL REFERENCES vehiculos(id),
    id_banco                BIGINT NOT NULL REFERENCES bancos(id),
    id_tasa                 BIGINT NOT NULL REFERENCES tasas_interes(id),

    moneda                  VARCHAR(10),
    precio_vehiculo         NUMERIC(14,2),
    cuota_inicial_pct       NUMERIC(6,2),
    cuota_inicial_monto     NUMERIC(14,2),
    tipo_tasa               VARCHAR(30),
    tasa_valor              NUMERIC(10,4),
    capitalizacion          VARCHAR(20),
    tem                     NUMERIC(14,10),
    gracia_tipo             VARCHAR(20),
    gracia_meses            INTEGER,
    tsd                     NUMERIC(10,4),
    tsv                     NUMERIC(10,4),
    portes                  NUMERIC(10,2),
    gastos_admin            NUMERIC(10,2) DEFAULT 0.0,
    gps                     NUMERIC(10,2) DEFAULT 0.0,
    metodo_pago             VARCHAR(30)   DEFAULT 'regular',
    pct_cuota_final         NUMERIC(6,4)  DEFAULT 0.0,
    cok                     NUMERIC(6,4)  DEFAULT 0.0,
    cronograma              JSONB
);

CREATE TABLE pagos (
    id                      BIGSERIAL PRIMARY KEY,
    num_cuota               INTEGER       NOT NULL DEFAULT 0,
    tipo_tasa               VARCHAR(30)   NOT NULL,
    tasa_interes            NUMERIC(10,4) NOT NULL DEFAULT 0.1,
    dias_capitalizacion     INTEGER       NOT NULL DEFAULT 1,
    dias_tasa               INTEGER       NOT NULL DEFAULT 360,
    fecha_pago              TIMESTAMP     NOT NULL DEFAULT now(),
    tipo_gracia             VARCHAR(20)   NOT NULL DEFAULT 'S',
    esta_pagado             BOOLEAN       NOT NULL DEFAULT FALSE,

    saldo_inicial_cf        NUMERIC(14,4) NOT NULL,
    interes_cf              NUMERIC(14,4) NOT NULL,
    amortizacion_cf         NUMERIC(14,4) NOT NULL,
    seguro_desgravamen_cf   NUMERIC(14,4) NOT NULL,
    saldo_final_cf          NUMERIC(14,4) NOT NULL,

    saldo_inicial           NUMERIC(14,4) NOT NULL,
    interes                 NUMERIC(14,4) NOT NULL,
    amortizacion            NUMERIC(14,4) NOT NULL,
    seguro_desgravamen      NUMERIC(14,4) NOT NULL,

    seguro_riesgo           NUMERIC(14,4) NOT NULL,
    portes                  NUMERIC(14,4) NOT NULL,
    gastos_admin            NUMERIC(14,4) NOT NULL,
    gps                     NUMERIC(14,4) NOT NULL DEFAULT 0.0,

    saldo_final             NUMERIC(14,4) NOT NULL,
    flujo                   NUMERIC(14,4) NOT NULL,

    id_simulacion           BIGINT NOT NULL REFERENCES simulaciones(id) ON DELETE CASCADE
);

CREATE INDEX idx_pagos_simulacion ON pagos(id_simulacion);
CREATE INDEX idx_simulaciones_cliente ON simulaciones(id_cliente);
CREATE INDEX idx_simulaciones_vehiculo ON simulaciones(id_vehiculo);
CREATE INDEX idx_simulaciones_elegido ON simulaciones(es_elegido);
CREATE INDEX idx_clientes_dni ON clientes(dni);
