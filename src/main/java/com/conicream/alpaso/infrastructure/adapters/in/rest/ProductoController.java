package com.conicream.alpaso.infrastructure.adapters.in.rest;

import com.conicream.alpaso.application.port.in.InventarioUseCase;
import com.conicream.alpaso.application.port.in.ProductoUseCase;
import com.conicream.alpaso.application.port.in.command.CorregirStockCommand;
import com.conicream.alpaso.application.port.in.command.EditarProductoCommand;
import com.conicream.alpaso.application.port.in.command.RegistrarProductoCommand;
import com.conicream.alpaso.application.port.in.command.ReponerStockCommand;
import com.conicream.alpaso.infrastructure.adapters.in.rest.dto.ProductoRequest;
import com.conicream.alpaso.infrastructure.adapters.in.rest.dto.ProductoResponse;
import com.conicream.alpaso.infrastructure.adapters.in.rest.dto.StockRequest;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


@RestController
@RequestMapping({"/api/v1/productos", "/api/productos"})
public class ProductoController {

    private final ProductoUseCase productos;
    private final InventarioUseCase inventario;

    public ProductoController(ProductoUseCase productos, InventarioUseCase inventario) {
        this.productos = productos;
        this.inventario = inventario;
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> registrar(@RequestBody ProductoRequest r) {
        var creado = productos.registrar(new RegistrarProductoCommand(
                r.codigo(), r.nombre(), r.marca(), r.precio(), r.unidadesIniciales(), r.stockMinimo()));
        return ResponseEntity.created(URI.create("/api/productos/" + creado.producto().getId()))
                .body(ProductoResponse.de(creado));
    }

    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(name = "soloActivos", defaultValue = "true") boolean soloActivos) {
        return productos.listar(soloActivos).stream().map(ProductoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ProductoResponse porId(@PathVariable long id) {
        return ProductoResponse.de(productos.porId(id));
    }

    @PutMapping("/{id}")
    public ProductoResponse editar(@PathVariable long id, @RequestBody ProductoRequest r) {
        return ProductoResponse.de(productos.editar(new EditarProductoCommand(
                id, r.codigo(), r.nombre(), r.marca(), r.precio())));
    }

    @DeleteMapping("/{id}")
    public ProductoResponse retirar(@PathVariable long id) {
        return ProductoResponse.de(productos.retirar(id));
    }

    @PatchMapping("/{id}/reactivar")
    public ProductoResponse reactivar(@PathVariable long id) {
        return ProductoResponse.de(productos.reactivar(id));
    }

    @GetMapping("/{id}/stock")
    public ProductoResponse consultarStock(@PathVariable long id) {
        return ProductoResponse.de(inventario.consultar(id));
    }

    @PostMapping("/{id}/stock/reposiciones")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN')")
    public ProductoResponse reponerStock(@PathVariable long id, @RequestBody StockRequest r) {
        return ProductoResponse.de(inventario.reponer(new ReponerStockCommand(id, r.unidades())));
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN')")
    public ProductoResponse corregirStock(@PathVariable long id, @RequestBody StockRequest r) {
        return ProductoResponse.de(inventario.corregir(new CorregirStockCommand(id, r.unidades())));
    }
}
