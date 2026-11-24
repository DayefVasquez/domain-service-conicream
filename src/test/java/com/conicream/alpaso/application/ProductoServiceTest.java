package com.conicream.alpaso.application;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.in.command.EditarProductoCommand;
import com.conicream.alpaso.application.port.in.command.RegistrarProductoCommand;
import com.conicream.alpaso.application.port.in.result.ProductoConStock;
import com.conicream.alpaso.domain.exception.ProductoInactivoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.vo.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Aplicación: ProductoService")
class ProductoServiceTest {

    @Test
    @DisplayName("Registra producto con código, stock inicial y stock mínimo")
    void registrarProducto() {
        Escenario e = new Escenario();
        ProductoConStock pc = e.productoService.registrar(new RegistrarProductoCommand(
                "CONO-001", "Cono Vainilla", "Crem", BigDecimal.valueOf(2800), 50, 10));

        assertEquals("CONO-001", pc.producto().getCodigo());
        assertEquals(50, pc.stock().unidadesDisponibles());
        assertEquals(10, pc.stock().getStockMinimo());
        assertTrue(pc.producto().isActivo());
    }

    @Test
    @DisplayName("Valida campos obligatorios al registrar")
    void validarCamposRegistro() {
        Escenario e = new Escenario();
        assertThrows(ValorInvalidoException.class, () -> e.productoService.registrar(
                new RegistrarProductoCommand("", null, null, BigDecimal.valueOf(24), 50, 5)));
    }

    @Test
    @DisplayName("Edita, retira y reactiva un producto")
    void editarRetirarReactivar() {
        Escenario e = new Escenario();
        long id = e.producto("DRAC-001", "Drácula", "Crem", 2500, 100, 20).producto().getId();

        ProductoConStock ed = e.productoService.editar(new EditarProductoCommand(id, "DRAC-002", "Drácula XL", "Crem", BigDecimal.valueOf(3000)));
        assertEquals("DRAC-002", ed.producto().getCodigo());
        assertEquals("Drácula XL", ed.producto().getNombre());
        // Comparamos usando Money.de() para alinear la escala monetaria a 2 decimales
        assertEquals(Money.de(3000).valor(), ed.producto().getPrecio().valor());

        // Retiro lógico
        e.productoService.retirar(id);
        assertFalse(e.productoService.porId(id).producto().isActivo());

        // Reactivación de temporada
        e.productoService.reactivar(id);
        assertTrue(e.productoService.porId(id).producto().isActivo());
    }
}
