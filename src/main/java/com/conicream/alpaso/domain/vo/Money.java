package com.conicream.alpaso.domain.vo;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object (Semana 6): inmutable, se compara por valor y nunca existe en estado inválido.
 * Usa BigDecimal con 2 decimales: nunca double para dinero.
 */
public record Money(BigDecimal valor) implements Comparable<Money> {

    public static final Money CERO = new Money(BigDecimal.ZERO);

    public Money {
        if (valor == null) {
            throw new ValorInvalidoException("El valor monetario es obligatorio");
        }
        if (valor.signum() < 0) {
            throw new ValorInvalidoException("El valor monetario no puede ser negativo");
        }
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    // Patrón Factory Method (Semana 7): nombres que dicen la intención.
    public static Money de(BigDecimal valor) {
        return new Money(valor);
    }

    public static Money de(long pesos) {
        return new Money(BigDecimal.valueOf(pesos));
    }

    public static Money de(String texto) {
        if (texto == null) {
            throw new ValorInvalidoException("El valor monetario es obligatorio");
        }
        try {
            return new Money(new BigDecimal(texto.trim()));
        } catch (NumberFormatException e) {
            throw new ValorInvalidoException("Valor monetario inválido: " + texto);
        }
    }

    public Money sumar(Money otro) {
        return new Money(valor.add(otro.valor));
    }

    public Money multiplicar(int factor) {
        if (factor < 0) {
            throw new ValorInvalidoException("El factor no puede ser negativo");
        }
        return new Money(valor.multiply(BigDecimal.valueOf(factor)));
    }

    public boolean esMayorQueCero() {
        return valor.signum() > 0;
    }

    @Override
    public int compareTo(Money otro) {
        return valor.compareTo(otro.valor);
    }
}
