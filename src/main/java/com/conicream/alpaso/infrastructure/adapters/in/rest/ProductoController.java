package com.conicream.alpaso.infrastructure.adapters.in.rest;

import com.conicream.alpaso.application.port.in.InventarioUseCase;
import com.conicream.alpaso.application.port.in.ProductoUseCase;
import com.conicream.alpaso.application.port.in.command.*;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.infrastructure.adapters.in.rest.dto.ProductoRequest;
import com.conicream.alpaso.infrastructure.adapters.in.rest.dto.ProductoResponse;
import com.conicream.alpaso.infrastructure.adapters.in.rest.dto.StockRequest;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Catálogo (HU-8) e inventario (HU-9). Ver: cualquier usuario autenticado. Cambiar: solo ADMIN (lo decide SecurityConfig). */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoUseCase productos;
    private final InventarioUseCase inventario;

    public ProductoController(ProductoUseCase productos, InventarioUseCase inventario) {
        this.productos = productos;
        this.inventario = inventario;
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> registrar(@RequestBody ProductoRequest r) {
        var creado = productos.registrar(new RegistrarProductoCommand(r.nombre(), r.marca(), r.precio(),
                r.unidadesPorCaja() == null ? 0 : r.unidadesPorCaja(),
                r.cajasIniciales() == null ? 0 : r.cajasIniciales()));
        return ResponseEntity.created(URI.create("/api/productos/" + creado.producto().getId())).body(ProductoResponse.de(creado));
    }

    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(defaultValue = "true") boolean soloActivos) {
        return productos.listar(soloActivos).stream().map(ProductoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable long id) {
        return ProductoResponse.de(productos.porId(id));
    }

    @PutMapping("/{id}")
    public ProductoResponse editar(@PathVariable long id, @RequestBody ProductoRequest r) {
        return ProductoResponse.de(productos.editar(new EditarProductoCommand(id, r.nombre(), r.marca(), r.precio())));
    }

    /** Baja lógica: el producto sigue existiendo para el historial de pedidos. */
    @DeleteMapping("/{id}")
    public ProductoResponse retirar(@PathVariable long id) {
        return ProductoResponse.de(productos.retirar(id));
    }

    @GetMapping("/{id}/stock")
    public ProductoResponse stock(@PathVariable long id) {
        return ProductoResponse.de(inventario.consultar(id));
    }

    @PostMapping("/{id}/stock/reposiciones")
    public ProductoResponse reponer(@PathVariable long id, @RequestBody StockRequest r) {
        if (r.cajas() == null) {
            throw new ValorInvalidoException("Indique cuántas cajas ingresaron");
        }
        return ProductoResponse.de(inventario.reponer(new ReponerStockCommand(id, r.cajas())));
    }

    @PutMapping("/{id}/stock")
    public ProductoResponse corregir(@PathVariable long id, @RequestBody StockRequest r) {
        return ProductoResponse.de(inventario.corregir(new CorregirStockCommand(id,
                r.cajas() == null ? 0 : r.cajas(), r.unidadesSueltas() == null ? 0 : r.unidadesSueltas())));
    }
}
