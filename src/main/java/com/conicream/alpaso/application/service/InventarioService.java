package com.conicream.alpaso.application.service;

import com.conicream.alpaso.application.port.in.InventarioUseCase;
import com.conicream.alpaso.application.port.in.command.CorregirStockCommand;
import com.conicream.alpaso.application.port.in.command.ReponerStockCommand;
import com.conicream.alpaso.application.port.in.result.ProductoConStock;
import com.conicream.alpaso.application.port.out.ProductoRepositoryPort;
import com.conicream.alpaso.application.port.out.StockRepositoryPort;
import com.conicream.alpaso.domain.exception.ProductoNoEncontradoException;
import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.model.StockProducto;

/** HU-9: control del inventario por cajas y unidades. */
public class InventarioService implements InventarioUseCase {

    private final ProductoRepositoryPort productos;
    private final StockRepositoryPort stocks;

    public InventarioService(ProductoRepositoryPort productos, StockRepositoryPort stocks) {
        this.productos = productos;
        this.stocks = stocks;
    }

    @Override
    public ProductoConStock consultar(long productoId) {
        return new ProductoConStock(producto(productoId), stock(productoId));
    }

    /** No se repone un producto retirado. */
    @Override
    public ProductoConStock reponer(ReponerStockCommand c) {
        Producto producto = producto(c.productoId());
        producto.asegurarActivo();
        StockProducto stock = stock(c.productoId());
        stock.reponerCajas(c.cajas());
        return new ProductoConStock(producto, stocks.guardar(stock));
    }

    /** El conteo manual sí se permite en un producto retirado: es la forma de dejar bien lo que quedó. */
    @Override
    public ProductoConStock corregir(CorregirStockCommand c) {
        Producto producto = producto(c.productoId());
        StockProducto stock = stock(c.productoId());
        stock.corregir(c.cajas(), c.unidadesSueltas());
        return new ProductoConStock(producto, stocks.guardar(stock));
    }

    private Producto producto(long id) {
        return productos.buscarPorId(id).orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    private StockProducto stock(long productoId) {
        return stocks.buscarPorProducto(productoId).orElseThrow(() -> new ProductoNoEncontradoException(productoId));
    }
}
