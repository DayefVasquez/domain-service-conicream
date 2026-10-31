package com.conicream.alpaso.infrastructure.config;

import com.conicream.alpaso.application.port.out.*;
import com.conicream.alpaso.application.service.*;
import com.conicream.alpaso.domain.service.LiquidacionPorVendidas;
import com.conicream.alpaso.domain.service.ReglaLiquidacion;
import com.conicream.alpaso.domain.service.ValidadorStock;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Raíz de composición. El dominio y la aplicación no llevan anotaciones de Spring (Semana 5, DIP):
 * es esta clase, en el borde, la que los construye y les entrega sus dependencias.
 */
@Configuration
public class BeanConfig {

    @Bean
    Clock reloj(@Value("${negocio.zona-horaria}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }

    @Bean
    ValidadorStock validadorStock() {
        return new ValidadorStock();
    }

    /** Estrategia vigente (OCP): para otra regla de cobro se escribe otra clase y se cambia SOLO esta línea. */
    @Bean
    ReglaLiquidacion reglaLiquidacion() {
        return new LiquidacionPorVendidas();
    }

    @Bean
    ProductoService productoService(ProductoRepositoryPort productos, StockRepositoryPort stocks, TransaccionPort tx) {
        return new ProductoService(productos, stocks, tx);
    }

    @Bean
    InventarioService inventarioService(ProductoRepositoryPort productos, StockRepositoryPort stocks) {
        return new InventarioService(productos, stocks);
    }

    @Bean
    PedidoService pedidoService(ProductoRepositoryPort productos, StockRepositoryPort stocks, PedidoRepositoryPort pedidos,
                                TransaccionPort tx, Clock reloj, ValidadorStock validador) {
        return new PedidoService(productos, stocks, pedidos, tx, reloj, validador);
    }

    @Bean
    LiquidacionService liquidacionService(PedidoRepositoryPort pedidos, LiquidacionRepositoryPort liquidaciones,
                                          TransaccionPort tx, ReglaLiquidacion regla) {
        return new LiquidacionService(pedidos, liquidaciones, tx, regla);
    }

    @Bean
    ReporteService reporteService(PedidoRepositoryPort pedidos, LiquidacionRepositoryPort liquidaciones,
                                  ReporteExportPort exportador, Clock reloj) {
        return new ReporteService(pedidos, liquidaciones, exportador, reloj);
    }
}
