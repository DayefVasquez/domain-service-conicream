package com.conicream.alpaso.application.service;

import com.conicream.alpaso.application.port.in.ProductoUseCase;
import com.conicream.alpaso.application.port.in.command.EditarProductoCommand;
import com.conicream.alpaso.application.port.in.command.RegistrarProductoCommand;
import com.conicream.alpaso.application.port.in.result.ProductoConStock;
import com.conicream.alpaso.application.port.out.ProductoRepositoryPort;
import com.conicream.alpaso.application.port.out.StockRepositoryPort;
import com.conicream.alpaso.application.port.out.TransaccionPort;
import com.conicream.alpaso.domain.exception.ProductoDuplicadoException;
import com.conicream.alpaso.domain.exception.ProductoNoEncontradoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.model.StockProducto;
import com.conicream.alpaso.domain.vo.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** HU-8: gestión de productos. Al crear un producto se crea también su stock inicial, en la misma operación. */
public class ProductoService implements ProductoUseCase {

    private final ProductoRepositoryPort productos;
    private final StockRepositoryPort stocks;
    private final TransaccionPort transaccion;

    public ProductoService(ProductoRepositoryPort productos, StockRepositoryPort stocks, TransaccionPort transaccion) {
        this.productos = productos;
        this.stocks = stocks;
        this.transaccion = transaccion;
    }

    @Override
    public ProductoConStock registrar(RegistrarProductoCommand c) {
        exigirCampos(c.nombre(), c.marca(), c.precio());
        Producto nuevo = Producto.registrar(c.nombre(), c.marca(), Money.de(c.precio()), c.unidadesPorCaja());
        StockProducto stockInicial = StockProducto.crear(0, nuevo.getUnidadesPorCaja(), c.cajasIniciales());   // valida las cajas antes de guardar
        asegurarNombreLibre(nuevo.getNombre(), nuevo.getMarca(), null);
        return transaccion.ejecutar(() -> {
            Producto guardado = productos.guardar(nuevo);
            StockProducto stock = stocks.guardar(StockProducto.reconstruir(guardado.getId(),
                    guardado.getUnidadesPorCaja(), stockInicial.unidadesDisponibles()));
            return new ProductoConStock(guardado, stock);
        });
    }

    @Override
    public ProductoConStock editar(EditarProductoCommand c) {
        exigirCampos(c.nombre(), c.marca(), c.precio());
        Producto producto = buscar(c.id());
        producto.editar(c.nombre(), c.marca(), Money.de(c.precio()));
        asegurarNombreLibre(producto.getNombre(), producto.getMarca(), producto.getId());
        return conStock(productos.guardar(producto));
    }

    @Override
    public ProductoConStock retirar(long id) {
        Producto producto = buscar(id);
        producto.retirar();
        return conStock(productos.guardar(producto));
    }

    @Override
    public ProductoConStock porId(long id) {
        return conStock(buscar(id));
    }

    @Override
    public List<ProductoConStock> listar(boolean soloActivos) {
        List<Producto> lista = productos.listar(soloActivos);
        Map<Long, StockProducto> porProducto = stocks.buscarPorProductos(lista.stream().map(Producto::getId).toList())
                .stream().collect(Collectors.toMap(StockProducto::getProductoId, Function.identity()));
        return lista.stream().map(p -> new ProductoConStock(p, porProducto.get(p.getId()))).toList();
    }

    private Producto buscar(long id) {
        return productos.buscarPorId(id).orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    private ProductoConStock conStock(Producto producto) {
        StockProducto stock = stocks.buscarPorProducto(producto.getId())
                .orElseThrow(() -> new ProductoNoEncontradoException(producto.getId()));
        return new ProductoConStock(producto, stock);
    }

    private void asegurarNombreLibre(String nombre, String marca, Long idPropio) {
        productos.buscarActivoPorNombreYMarca(nombre, marca)
                .filter(otro -> !otro.getId().equals(idPropio))
                .ifPresent(otro -> { throw new ProductoDuplicadoException(nombre, marca); });
    }

    /** Lista todos los campos faltantes juntos, para que quien llama corrija de una vez. */
    private static void exigirCampos(String nombre, String marca, BigDecimal precio) {
        List<String> faltan = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) faltan.add("nombre");
        if (marca == null || marca.isBlank()) faltan.add("marca");
        if (precio == null) faltan.add("precio");
        if (!faltan.isEmpty()) {
            throw new ValorInvalidoException("Faltan campos obligatorios: " + String.join(", ", faltan));
        }
    }
}
