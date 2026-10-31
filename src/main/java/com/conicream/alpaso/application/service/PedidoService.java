package com.conicream.alpaso.application.service;

import com.conicream.alpaso.application.port.in.PedidoUseCase;
import com.conicream.alpaso.application.port.in.Solicitante;
import com.conicream.alpaso.application.port.in.command.CrearPedidoCommand;
import com.conicream.alpaso.application.port.out.PedidoRepositoryPort;
import com.conicream.alpaso.application.port.out.ProductoRepositoryPort;
import com.conicream.alpaso.application.port.out.StockRepositoryPort;
import com.conicream.alpaso.application.port.out.TransaccionPort;
import com.conicream.alpaso.domain.exception.AccesoDenegadoException;
import com.conicream.alpaso.domain.exception.PedidoNoEncontradoException;
import com.conicream.alpaso.domain.exception.ProductoNoEncontradoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.model.StockProducto;
import com.conicream.alpaso.domain.service.ValidadorStock;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** HU-10: crear y confirmar pedidos. Crear valida contra el stock; confirmar lo vuelve a validar y lo descuenta. */
public class PedidoService implements PedidoUseCase {

    private final ProductoRepositoryPort productos;
    private final StockRepositoryPort stocks;
    private final PedidoRepositoryPort pedidos;
    private final TransaccionPort transaccion;
    private final Clock reloj;
    private final ValidadorStock validadorStock;

    public PedidoService(ProductoRepositoryPort productos, StockRepositoryPort stocks, PedidoRepositoryPort pedidos,
                         TransaccionPort transaccion, Clock reloj, ValidadorStock validadorStock) {
        this.productos = productos;
        this.stocks = stocks;
        this.pedidos = pedidos;
        this.transaccion = transaccion;
        this.reloj = reloj;
        this.validadorStock = validadorStock;
    }

    @Override
    public Pedido crear(CrearPedidoCommand c) {
        if (c.lineas() == null || c.lineas().isEmpty()) {
            throw new ValorInvalidoException("El pedido debe tener al menos una línea");
        }
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (CrearPedidoCommand.Linea l : c.lineas()) {
            if (l.cantidad() <= 0) {
                throw new ValorInvalidoException("La cantidad del producto " + l.productoId() + " debe ser mayor que cero");
            }
            try {
                cantidades.merge(l.productoId(), l.cantidad(), Math::addExact);
            } catch (ArithmeticException e) {
                throw new ValorInvalidoException("La cantidad del producto " + l.productoId() + " es demasiado grande");
            }
        }
        Map<Long, Producto> catalogo = cargarProductos(cantidades.keySet());
        List<DetallePedido> detalles = new ArrayList<>();
        cantidades.forEach((id, cantidad) -> {
            Producto p = catalogo.get(id);
            p.asegurarActivo();
            detalles.add(new DetallePedido(id, p.nombreCompleto(), cantidad, p.getPrecio()));
        });
        validadorStock.validar(detalles, cargarStocks(cantidades.keySet()));
        return pedidos.guardar(Pedido.crear(c.solicitante().id(), LocalDate.now(reloj), detalles));
    }

    @Override
    public Pedido confirmar(Solicitante solicitante, long pedidoId) {
        return transaccion.ejecutar(() -> {
            Pedido pedido = cargar(solicitante, pedidoId);
            pedido.confirmar();   // falla si no está en BORRADOR, antes de tocar el stock
            Set<Long> ids = pedido.getDetalles().stream().map(DetallePedido::productoId).collect(Collectors.toSet());
            cargarProductos(ids).values().forEach(Producto::asegurarActivo);
            Map<Long, StockProducto> stock = cargarStocks(ids);
            validadorStock.validar(pedido.getDetalles(), stock);   // valida TODO antes de descontar algo
            for (DetallePedido d : pedido.getDetalles()) {
                StockProducto s = stock.get(d.productoId());
                s.descontar(d.cantidad());
                stocks.guardar(s);
            }
            return pedidos.guardar(pedido);
        });
    }

    @Override
    public Pedido porId(Solicitante solicitante, long pedidoId) {
        return cargar(solicitante, pedidoId);
    }

    private Pedido cargar(Solicitante solicitante, long pedidoId) {
        Pedido pedido = pedidos.buscarPorId(pedidoId).orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
        if (!solicitante.puedeVerPedidoDe(pedido.getComercianteId())) {
            throw new AccesoDenegadoException();
        }
        return pedido;
    }

    private Map<Long, Producto> cargarProductos(Set<Long> ids) {
        Map<Long, Producto> encontrados = productos.buscarPorIds(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
        for (Long id : ids) {
            if (!encontrados.containsKey(id)) {
                throw new ProductoNoEncontradoException(id);
            }
        }
        return encontrados;
    }

    private Map<Long, StockProducto> cargarStocks(Set<Long> ids) {
        return stocks.buscarPorProductos(ids).stream()
                .collect(Collectors.toMap(StockProducto::getProductoId, Function.identity()));
    }
}
