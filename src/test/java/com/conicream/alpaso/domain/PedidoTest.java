package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.domain.exception.PedidoEstadoInvalidoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.testsupport.Datos;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PedidoTest {

    private DetallePedido linea(long producto, int cantidad, long precio) {
        return new DetallePedido(producto, "Producto " + producto, cantidad, Money.de(precio));
    }

    @Test
    @DisplayName("El total es la suma de cantidad × precio de cada línea")
    void total() {
        Pedido p = Pedido.crear("111", Datos.HOY, List.of(linea(1, 10, 2500), linea(2, 4, 3000)));
        assertEquals(Money.de(37000), p.total());
        assertEquals(EstadoPedido.BORRADOR, p.getEstado());
        assertNull(p.getId());
    }

    @Test
    void unProductoRepetidoSeSumaEnUnaSolaLinea() {
        Pedido p = Pedido.crear("111", Datos.HOY, List.of(linea(1, 10, 2500), linea(1, 5, 2500)));
        assertEquals(1, p.getDetalles().size());
        assertEquals(15, p.getDetalles().get(0).cantidad());
    }

    @Test
    void elMismoProductoConDosPreciosSeRechaza() {
        assertThrows(ValorInvalidoException.class,
                () -> Pedido.crear("111", Datos.HOY, List.of(linea(1, 1, 2500), linea(1, 1, 2600))));
    }

    @Test
    void validaDatosObligatorios() {
        assertThrows(ValorInvalidoException.class, () -> Pedido.crear("111", Datos.HOY, List.of()));
        assertThrows(ValorInvalidoException.class, () -> Pedido.crear(" ", Datos.HOY, List.of(linea(1, 1, 1))));
        assertThrows(ValorInvalidoException.class, () -> Pedido.crear("111", null, List.of(linea(1, 1, 1))));
        assertThrows(ValorInvalidoException.class, () -> linea(1, 0, 100));
        assertThrows(ValorInvalidoException.class, () -> linea(1, -2, 100));
    }

    @Test
    @DisplayName("Ciclo BORRADOR → CONFIRMADO → LIQUIDADO, sin saltos ni retrocesos")
    void ciclo() {
        Pedido p = Pedido.reconstruir(1, "111", Datos.HOY, EstadoPedido.BORRADOR, List.of(linea(1, 1, 100)));
        assertThrows(PedidoEstadoInvalidoException.class, p::marcarLiquidado);
        p.confirmar();
        assertEquals(EstadoPedido.CONFIRMADO, p.getEstado());
        assertThrows(PedidoEstadoInvalidoException.class, p::confirmar);
        p.marcarLiquidado();
        assertEquals(EstadoPedido.LIQUIDADO, p.getEstado());
        assertThrows(PedidoEstadoInvalidoException.class, p::marcarLiquidado);
        assertThrows(PedidoEstadoInvalidoException.class, p::confirmar);
    }

    @Test
    void perteneceAUnComerciante() {
        Pedido p = Pedido.crear("111", Datos.HOY, List.of(linea(1, 1, 100)));
        assertTrue(p.perteneceA("111"));
        assertFalse(p.perteneceA("222"));
    }
}
