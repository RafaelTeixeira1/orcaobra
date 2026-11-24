package br.com.orcaobra.dominio.comum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object para percentuais. Guarda a fração (10% = 0,10) com 6 casas.
 */
public record Percentual(BigDecimal fracao) implements Comparable<Percentual> {

    public static final Percentual ZERO = new Percentual(BigDecimal.ZERO);
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    public Percentual {
        Objects.requireNonNull(fracao, "fração é obrigatória");
        fracao = fracao.setScale(6, RoundingMode.HALF_EVEN);
    }

    /** Cria a partir do valor em pontos percentuais: {@code de("10")} = 10%. */
    public static Percentual de(String pontosPercentuais) {
        return de(new BigDecimal(pontosPercentuais));
    }

    public static Percentual de(BigDecimal pontosPercentuais) {
        return new Percentual(pontosPercentuais.divide(CEM, 6, RoundingMode.HALF_EVEN));
    }

    /** Valor em pontos percentuais com 2 casas (0,1234 → 12,34). */
    public BigDecimal emPontos() {
        return fracao.multiply(CEM).setScale(2, RoundingMode.HALF_EVEN);
    }

    public boolean menorQue(Percentual outro) {
        return compareTo(outro) < 0;
    }

    @Override
    public int compareTo(Percentual outro) {
        return fracao.compareTo(outro.fracao);
    }

    @Override
    public String toString() {
        return emPontos().toPlainString().replace('.', ',') + "%";
    }
}
