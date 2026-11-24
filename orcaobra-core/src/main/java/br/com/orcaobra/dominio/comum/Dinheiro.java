package br.com.orcaobra.dominio.comum;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Value Object que representa um valor monetário em reais (2 casas decimais).
 * Imutável: toda operação devolve uma nova instância.
 */
public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);
    private static final Locale PT_BR = Locale.of("pt", "BR");

    public Dinheiro {
        Objects.requireNonNull(valor, "valor é obrigatório");
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public static Dinheiro de(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public static Dinheiro de(long valor) {
        return new Dinheiro(BigDecimal.valueOf(valor));
    }

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    public Dinheiro subtrair(Dinheiro outro) {
        return new Dinheiro(valor.subtract(outro.valor));
    }

    public Dinheiro multiplicar(BigDecimal fator) {
        return new Dinheiro(valor.multiply(fator, MathContext.DECIMAL64));
    }

    public Dinheiro multiplicar(Percentual percentual) {
        return multiplicar(percentual.fracao());
    }

    /** Quanto este valor representa de {@code total}. Retorna zero se o total for zero. */
    public Percentual proporcaoDe(Dinheiro total) {
        if (total.ehZero()) {
            return Percentual.ZERO;
        }
        return new Percentual(valor.divide(total.valor, 6, RoundingMode.HALF_EVEN));
    }

    public boolean ehZero() {
        return valor.signum() == 0;
    }

    public boolean ehNegativo() {
        return valor.signum() < 0;
    }

    public boolean maiorQue(Dinheiro outro) {
        return compareTo(outro) > 0;
    }

    public static Dinheiro max(Dinheiro a, Dinheiro b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public String formatado() {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return formatado();
    }
}
