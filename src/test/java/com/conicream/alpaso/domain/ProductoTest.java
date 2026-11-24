package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.domain.exception.ProductoInactivoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dominio: Producto de catálogo")
class ProductoTest {

    @Test
    @DisplayName("Crea producto activo con código de fábrica")
    void crearProducto() {
        Producto p = Producto.registrar("CHOC-001", "Chococono", "Crem Helado", Money.de(3200));
        assertEquals("CHOC-001", p.getCodigo());
        assertEquals("Chococono", p.getNombre());
        assertEquals("Crem Helado", p.getMarca());
        assertEquals(Money.de(3200), p.getPrecio());
        assertTrue(p.isActivo());
    }

    @Test
    @DisplayName("Valida invariantes de texto y precio")
    void validacionesInvariantes() {
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("", "N", "M", Money.de(100)));
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("C", " ", "M", Money.de(100)));
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("C", "N", " ", Money.de(100)));
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("C", "N", "M", null));
        assertThrows(ValorInvalidoException.class, () -> Producto.registrar("C", "N", "M", Money.CERO));
    }

    @Test
    @DisplayName("Edita datos de un producto activo")
    void editarProducto() {
        Producto p = Producto.registrar("CHOC-001", "Chococono", "Crem Helado", Money.de(3200));
        p.editar("CHOC-002", "Chococono Crocante", "Crem Helado", Money.de(3500));
        assertEquals("CHOC-002", p.getCodigo());
        assertEquals("Chococono Crocante", p.getNombre());
        assertEquals(Money.de(3500), p.getPrecio());
    }

    @Test
    @DisplayName("Retiro y reactivación reversible para temporadas")
    void cicloDeTemporada() {
        Producto p = Producto.registrar("DRAC-001", "Paleta Drácula", "Crem Helado", Money.de(2500));
        assertTrue(p.isActivo());

        // Termina temporada -> Se retira
        p.retirar();
        assertFalse(p.isActivo());
        assertThrows(ProductoInactivoException.class, () -> p.editar("D", "D", "M", Money.de(100)));

        // Vuelve temporada -> Se reactiva exitosamente
        p.reactivar();
        assertTrue(p.isActivo());
    }
}
