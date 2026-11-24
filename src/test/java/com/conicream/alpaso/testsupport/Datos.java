package com.conicream.alpaso.testsupport;

import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.vo.Money;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Datos de ejemplo para pruebas con reloj fijo en Bogotá. */
public final class Datos {

    public static final LocalDate HOY = LocalDate.of(2026, 10, 20);

    private Datos() { }

    public static Clock relojFijo() {
        return Clock.fixed(Instant.parse("2026-10-20T15:00:00Z"), ZoneId.of("America/Bogota"));
    }

    public static Producto cono() {
        return Producto.registrar("CONO-001", "Cono Vainilla", "Crem Helado", Money.de(2500));
    }
}
