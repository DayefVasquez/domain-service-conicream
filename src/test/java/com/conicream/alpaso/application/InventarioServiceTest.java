package com.conicream.alpaso.application;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.in.command.CorregirStockCommand;
import com.conicream.alpaso.application.port.in.command.ReponerStockCommand;
import com.conicream.alpaso.domain.exception.ProductoInactivoException;
import com.conicream.alpaso.domain.exception.ProductoNoEncontradoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventarioServiceTest {

    private final Escenario e = new Escenario();

    private long productoConDosCajas() {
        return e.producto("Cono", "Crem", 2500, 24, 2).producto().getId().longValue();
    }

    @Test
    void consultaCajasYUnidades() {
        long id = productoConDosCajas();
        var s = e.inventarioService.consultar(id).stock();
        assertEquals(2, s.cajasCompletas());
        assertEquals(48, s.unidadesDisponibles());
        assertThrows(ProductoNoEncontradoException.class, () -> e.inventarioService.consultar(99));
    }

    @Test
    @DisplayName("Reponer suma cajas y queda guardado")
    void repone() {
        long id = productoConDosCajas();
        assertEquals(120, e.inventarioService.reponer(new ReponerStockCommand(id, 3)).stock().unidadesDisponibles());
        assertEquals(120, e.stockDe(id));
        assertThrows(ValorInvalidoException.class, () -> e.inventarioService.reponer(new ReponerStockCommand(id, 0)));
        assertEquals(120, e.stockDe(id));
    }

    @Test
    void noSeReponeUnProductoRetirado() {
        long id = productoConDosCajas();
        e.productoService.retirar(id);
        assertThrows(ProductoInactivoException.class, () -> e.inventarioService.reponer(new ReponerStockCommand(id, 1)));
        assertThrows(ProductoNoEncontradoException.class, () -> e.inventarioService.reponer(new ReponerStockCommand(99, 1)));
    }

    @Test
    @DisplayName("El conteo manual fija el stock, incluso en un producto retirado")
    void corrige() {
        long id = productoConDosCajas();
        assertEquals(53, e.inventarioService.corregir(new CorregirStockCommand(id, 2, 5)).stock().unidadesDisponibles());
        e.productoService.retirar(id);
        assertEquals(24, e.inventarioService.corregir(new CorregirStockCommand(id, 1, 0)).stock().unidadesDisponibles());
        assertThrows(ValorInvalidoException.class, () -> e.inventarioService.corregir(new CorregirStockCommand(id, 1, 24)));
        assertEquals(24, e.stockDe(id));
    }
}
