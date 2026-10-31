package com.conicream.alpaso.application;

import static com.conicream.alpaso.application.Escenario.*;
import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.in.command.LiquidarPedidoCommand;
import com.conicream.alpaso.application.port.in.result.ArchivoReporte;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.testsupport.Datos;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReporteServiceTest {

    private final Escenario e = new Escenario();

    @Test
    @DisplayName("Sin fecha usa hoy; el nombre lleva la fecha y los borradores no aparecen")
    void reporteDeHoy() {
        long cono = e.producto("Cono", "Crem", 2500, 24, 10).producto().getId().longValue();
        long liquidado = e.pedido(ANA, cono, 100).getId().longValue();
        e.pedidoService.confirmar(ANA, liquidado);
        e.liquidacionService.liquidar(new LiquidarPedidoCommand(ANA, liquidado, Map.of(cono, 20)));
        long pendiente = e.pedido(LUIS, cono, 50).getId().longValue();
        e.pedidoService.confirmar(LUIS, pendiente);
        e.pedido(LUIS, cono, 10);   // borrador

        ArchivoReporte a = e.reporteService.generarDiario(null);

        assertEquals("reporte-diario-2026-10-20.xlsx", a.nombre());
        assertTrue(a.contenido().length > 0);
        assertEquals(Datos.HOY, e.exportador.recibido.fecha());
        assertEquals(2, e.exportador.recibido.totalPedidos());
        assertEquals(1, e.exportador.recibido.pedidosLiquidados());
        assertEquals(Money.de(200000), e.exportador.recibido.totalLiquidado());
    }

    @Test
    @DisplayName("Un día sin pedidos genera el archivo con todo en cero, no un error")
    void diaSinPedidos() {
        ArchivoReporte a = e.reporteService.generarDiario(Datos.HOY.minusDays(3));
        assertEquals("reporte-diario-2026-10-17.xlsx", a.nombre());
        assertEquals(0, e.exportador.recibido.totalPedidos());
        assertEquals(Money.CERO, e.exportador.recibido.totalLiquidado());
    }

    @Test
    void unaFechaFuturaSeRechaza() {
        assertThrows(ValorInvalidoException.class, () -> e.reporteService.generarDiario(Datos.HOY.plusDays(1)));
        e.reporteService.generarDiario(Datos.HOY);   // hoy sí
    }
}
