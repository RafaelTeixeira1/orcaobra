package br.com.orcaobra.dominio.servico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de uma calculadora: quanto de cada material e quantas horas de cada função
 * o serviço consome. Ainda sem preços — os preços vêm do catálogo.
 */
public record ComposicaoServico(List<Consumo> materiais, List<Consumo> maoDeObra) {

    /** Consumo de um insumo do catálogo, identificado pelo código. */
    public record Consumo(String codigo, BigDecimal quantidade) {
    }

    public ComposicaoServico {
        materiais = List.copyOf(materiais);
        maoDeObra = List.copyOf(maoDeObra);
    }

    public static Builder nova() {
        return new Builder();
    }

    /** Builder: deixa as calculadoras legíveis, uma linha por insumo. */
    public static final class Builder {
        private final List<Consumo> materiais = new ArrayList<>();
        private final List<Consumo> maoDeObra = new ArrayList<>();

        private Builder() {
        }

        public Builder material(String codigo, BigDecimal quantidade) {
            materiais.add(new Consumo(codigo, arredondar(quantidade)));
            return this;
        }

        public Builder horas(String codigoFuncao, BigDecimal horas) {
            maoDeObra.add(new Consumo(codigoFuncao, arredondar(horas)));
            return this;
        }

        public ComposicaoServico build() {
            return new ComposicaoServico(materiais, maoDeObra);
        }

        private static BigDecimal arredondar(BigDecimal v) {
            return v.setScale(4, RoundingMode.HALF_UP);
        }
    }
}
