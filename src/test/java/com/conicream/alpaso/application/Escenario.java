package com.conicream.alpaso.application;

import com.conicream.alpaso.application.port.in.Solicitante;
import com.conicream.alpaso.application.port.in.command.CrearPedidoCommand;
import com.conicream.alpaso.application.port.in.command.RegistrarProductoCommand;
import com.conicream.alpaso.application.port.in.result.ProductoConStock;
import com.conicream.alpaso.application.service.*;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.service.LiquidacionPorVendidas;
import com.conicream.alpaso.domain.service.ValidadorStock;
import com.conicream.alpaso.testsupport.*;
import java.math.BigDecimal;
import java.util.List;

final class Escenario {

    static final Solicitante ADMIN = new Solicitante("admin", true);
    static final Solicitante ANA = new Solicitante("1090123456", false);
    static final Solicitante LUIS = new Solicitante("1090999888", false);

    final ProductoRepositoryEnMemoria productos = new ProductoRepositoryEnMemoria();
    final StockRepositoryEnMemoria stocks = new StockRepositoryEnMemoria();
    final PedidoRepositoryEnMemoria pedidos = new PedidoRepositoryEnMemoria();
    final LiquidacionRepositoryEnMemoria liquidaciones = new LiquidacionRepositoryEnMemoria();
    final ReporteExportFalso exportador = new ReporteExportFalso();
    final TransaccionDirecta tx = new TransaccionDirecta();

    final ProductoService productoService = new ProductoService(productos, stocks, tx);
    final InventarioService inventarioService = new InventarioService(productos, stocks);
    final PedidoService pedidoService = new PedidoService(productos, stocks, pedidos, tx, Datos.relojFijo(), new ValidadorStock());

    final LiquidacionService liquidacionService = new LiquidacionService(
            pedidos, liquidaciones, stocks, tx, new LiquidacionPorVendidas(), liq -> new byte[]{1, 2, 3});

    final ReporteService reporteService = new ReporteService(pedidos, liquidaciones, exportador, Datos.relojFijo());

    ProductoConStock producto(String codigo, String nombre, String marca, long precio, int unidadesTotales, int stockMinimo) {
        return productoService.registrar(new RegistrarProductoCommand(
                codigo, nombre, marca, BigDecimal.valueOf(precio), unidadesTotales, stockMinimo));
    }

    // Adaptador de compatibilidad para las pruebas existentes que usaban el formato de cajas del profesor
    ProductoConStock producto(String nombre, String marca, long precio, int unidadesPorCaja, int cajasIniciales) {
        String cod = "PROD-" + Math.abs((nombre + marca).hashCode() % 10000);
        int unidadesTotales = unidadesPorCaja * cajasIniciales; // Convierte las cajas de los tests a unidades puras
        return producto(cod, nombre, marca, precio, unidadesTotales, 5);
    }

    Pedido pedido(Solicitante dueño, long productoId, int cantidad) {
        return pedidoService.crear(new CrearPedidoCommand(dueño, List.of(new CrearPedidoCommand.Linea(productoId, cantidad))));
    }

    int stockDe(long productoId) {
        return stocks.buscarPorProducto(productoId).orElseThrow().unidadesDisponibles();
    }
}
