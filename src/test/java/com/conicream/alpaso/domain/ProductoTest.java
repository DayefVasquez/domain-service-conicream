package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.domain.exception.ProductoInactivoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.testsupport.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductoTest {

    @Test
    void nuevoNaceActivoYSinId() {
        Producto p = Datos.cono();
        assertTrue(p.isActivo());
        assertNull(p.getId());
        assertEquals("Cono Vainilla (Crem Helado)", p.nombreCompleto());
    }

    @Test
    @DisplayName("Valida nombre, marca, precio y unidades por caja")
    void validaCampos() {
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar(" ", "M", Money.de(1), 1));
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("N", null, Money.de(1), 1));
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("N", "M", Money.CERO, 1));
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("N", "M", null, 1));
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("N", "M", Money.de(1), 0));
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("x".repeat(101), "M", Money.de(1), 1));
    }

    @Test
    void editaNombreMarcaYPrecioPeroNoLasUnidadesPorCaja() {
        Producto p = Datos.cono();
        p.editar("Cono Chocolate", "Otra", Money.de(3000));
        assertEquals("Cono Chocolate", p.getNombre());
        assertEquals(Money.de(3000), p.getPrecio());
        assertEquals(24, p.getUnidadesPorCaja());
    }

    @Test
    @DisplayName("Un producto retirado queda inactivo y ya no se edita ni se retira otra vez")
    void retirar() {
        Producto p = Datos.cono();
        p.retirar();
        assertFalse(p.isActivo());
        assertThrows(ProductoInactivoException.class, () -> p.editar("X", "Y", Money.de(1)));
        assertThrows(ProductoInactivoException.class, p::retirar);
        assertThrows(ProductoInactivoException.class, p::asegurarActivo);
    }
}
