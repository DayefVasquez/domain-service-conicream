-- ============================================================================
-- 02_seed.sql - Datos Iniciales de Coni-Cream (Catálogo, Stock y Kardex)
-- Precios expresados en Pesos Colombianos (COP) - Precio de liquidación al vendedor
-- ============================================================================

-- 1. CATÁLOGO DE PRODUCTOS
INSERT INTO producto (codigo, nombre, marca, precio, activo) VALUES
('CHOC-001', 'Chococono Clásico', 'Crem Helado', 3200.00, TRUE),
('DRAC-002', 'Paleta Drácula', 'Crem Helado', 2500.00, TRUE),
('CONO-003', 'Cono Vainilla', 'Crem Helado', 2800.00, TRUE),
('LIMO-004', 'Paleta de Agua Limón', 'Crem Helado', 1500.00, TRUE),
('BOCC-005', 'Bocatto Frutos Rojos', 'Crem Helado', 4000.00, TRUE),
('NAVI-006', 'Paleta Árbol Navidad', 'Crem Helado', 3000.00, FALSE); -- Inactivo (Fuera de temporada)

-- 2. STOCK EN BODEGA (En Unidades Directas)
-- Chococono: 150 unidades disponibles (Stock normal)
INSERT INTO stock_producto (producto_id, unidades, stock_minimo) VALUES (1, 150, 30);

-- Paleta Drácula: 8 unidades disponibles (STOCK BAJO -> Alerta para fábrica)
INSERT INTO stock_producto (producto_id, unidades, stock_minimo) VALUES (2, 8, 20);

-- Cono Vainilla: 90 unidades disponibles
INSERT INTO stock_producto (producto_id, unidades, stock_minimo) VALUES (3, 90, 25);

-- Paleta Limón: 60 unidades disponibles
INSERT INTO stock_producto (producto_id, unidades, stock_minimo) VALUES (4, 60, 20);

-- Bocatto: 45 unidades disponibles
INSERT INTO stock_producto (producto_id, unidades, stock_minimo) VALUES (5, 45, 15);

-- Paleta Navidad: 0 unidades (Inactivo, no debe disparar alerta de stock bajo)
INSERT INTO stock_producto (producto_id, unidades, stock_minimo) VALUES (6, 0, 10);

-- 3. HISTORIAL DE KARDEX INICIAL (Registro de entrada de camión de fábrica)
INSERT INTO movimientos_inventario (producto_id, tipo_movimiento, unidades, observaciones) VALUES
(1, 'ENTRADA_REPOSICION', 150, 'Cargue inicial de fábrica - Camión semana 1'),
(2, 'ENTRADA_REPOSICION', 8,   'Remanente temporada Halloween'),
(3, 'ENTRADA_REPOSICION', 90,  'Cargue inicial de fábrica - Camión semana 1'),
(4, 'ENTRADA_REPOSICION', 60,  'Cargue inicial de fábrica - Camión semana 1'),
(5, 'ENTRADA_REPOSICION', 45,  'Cargue inicial de fábrica - Camión semana 1');
