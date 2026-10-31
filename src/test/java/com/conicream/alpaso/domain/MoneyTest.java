package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.vo.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    @DisplayName("Se compara por valor, sin importar cómo se construyó")
    void igualdadPorValor() {
        assertEquals(Money.de(1000), Money.de("1000"));
        assertEquals(Money.de(1000), Money.de(new BigDecimal("1000.00")));
    }

    @Test
    @DisplayName("Siempre queda con 2 decimales")
    void redondeaADosDecimales() {
        assertEquals(new BigDecimal("10.13"), Money.de("10.125").valor());
        assertEquals(new BigDecimal("5.00"), Money.de(5).valor());
    }

    @Test
    void sumaYMultiplica() {
        assertEquals(Money.de(15000), Money.de(5000).multiplicar(3));
        assertEquals(Money.de(7500), Money.de(5000).sumar(Money.de(2500)));
    }

    @Test
    @DisplayName("No existe dinero negativo ni texto que no sea número")
    void rechazaValoresInvalidos() {
        assertThrows(ValorInvalidoException.class, () -> Money.de(-1));
        assertThrows(ValorInvalidoException.class, () -> Money.de((BigDecimal) null));
        assertThrows(ValorInvalidoException.class, () -> Money.de("abc"));
        assertThrows(ValorInvalidoException.class, () -> Money.de(10).multiplicar(-2));
    }
}
