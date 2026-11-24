-- ============================================================================
-- 01_schema.sql - BASE DE DATOS: core_db (PostgreSQL 17)
-- Totalmente sincronizado con las entidades JPA de Arquitectura Hexagonal
-- ============================================================================

-- 1. CATÁLOGO DE PRODUCTOS (Borrado lógico con 'activo', único precio base)
CREATE TABLE IF NOT EXISTS producto (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    marca VARCHAR(80) NOT NULL,
    precio NUMERIC(14, 2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_producto_precio CHECK (precio > 0)
);
CREATE INDEX IF NOT EXISTS ix_producto_activo ON producto (activo);

-- 2. STOCK EN BODEGA (En unidades puras + Umbral de alerta mínima)
CREATE TABLE IF NOT EXISTS stock_producto (
    producto_id BIGINT PRIMARY KEY,
    unidades INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 5,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_stock_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE CASCADE,
    CONSTRAINT chk_stock_unidades CHECK (unidades >= 0),
    CONSTRAINT chk_stock_minimo CHECK (stock_minimo >= 0)
);

-- 3. KARDEX DE BODEGA (Trazabilidad de entradas y bajas)
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL,
    unidades INTEGER NOT NULL,
    fecha_hora TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,

    CONSTRAINT fk_movimientos_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_movimientos_tipo CHECK (tipo_movimiento IN (
        'ENTRADA_REPOSICION',
        'BAJA_AVERIA',
        'BAJA_VENCIMIENTO',
        'AJUSTE_CONTEO'
    )),
    CONSTRAINT chk_movimientos_unidades CHECK (unidades > 0)
);

-- 4. COMANDA / PEDIDO DIARIO (Patrón Cuenta Abierta)
CREATE TABLE IF NOT EXISTS pedido (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    comerciante_id VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(15) NOT NULL DEFAULT 'BORRADOR',
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_pedido_estado CHECK (estado IN ('BORRADOR', 'CONFIRMADO', 'LIQUIDADO'))
);
CREATE INDEX IF NOT EXISTS ix_pedido_fecha ON pedido (fecha);
CREATE INDEX IF NOT EXISTS ix_pedido_comerciante ON pedido (comerciante_id);

-- 5. LÍNEAS DE LA COMANDA (@ElementCollection: orden + producto_id)
CREATE TABLE IF NOT EXISTS pedido_detalle (
    pedido_id BIGINT NOT NULL,
    orden INTEGER NOT NULL,
    producto_id BIGINT NOT NULL,
    nombre_producto VARCHAR(210) NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(14, 2) NOT NULL,

    CONSTRAINT pk_pedido_detalle PRIMARY KEY (pedido_id, orden),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario > 0)
);

-- 6. LIQUIDACIÓN DE JORNADA (Relación 1 a 1 estricta con el Pedido)
CREATE TABLE IF NOT EXISTS liquidacion (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    comerciante_id VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_liquidacion_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_liquidacion_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido (id) ON DELETE RESTRICT
);

-- 7. DETALLE DE LIQUIDACIÓN (@ElementCollection: despachadas vs sobrantes devueltos)
CREATE TABLE IF NOT EXISTS liquidacion_linea (
    liquidacion_id BIGINT NOT NULL,
    orden INTEGER NOT NULL,
    producto_id BIGINT NOT NULL,
    nombre_producto VARCHAR(210) NOT NULL,
    despachadas INTEGER NOT NULL,
    sobrantes INTEGER NOT NULL,
    precio_unitario NUMERIC(14, 2) NOT NULL,

    CONSTRAINT pk_liquidacion_linea PRIMARY KEY (liquidacion_id, orden),
    CONSTRAINT fk_linea_liquidacion FOREIGN KEY (liquidacion_id)
        REFERENCES liquidacion (id) ON DELETE CASCADE,
    CONSTRAINT fk_linea_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_linea_despachadas CHECK (despachadas > 0),
    CONSTRAINT chk_linea_sobrantes CHECK (sobrantes >= 0 AND sobrantes <= despachadas),
    CONSTRAINT chk_linea_precio CHECK (precio_unitario > 0)
);

-- 8. VISTA DE CONSULTA (Calcula el Total Factura al vuelo sin violar la 3NF)
CREATE OR REPLACE VIEW v_liquidacion_resumen AS
SELECT 
    l.id AS liquidacion_id,
    l.pedido_id,
    l.comerciante_id,
    SUM(ll.despachadas - ll.sobrantes) AS total_unidades_vendidas,
    SUM((ll.despachadas - ll.sobrantes) * ll.precio_unitario) AS total_factura_cobrado
FROM liquidacion l
JOIN liquidacion_linea ll ON l.id = ll.liquidacion_id
GROUP BY l.id, l.pedido_id, l.comerciante_id;
