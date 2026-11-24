package br.com.orcaobra.dominio.orcamento;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Unidade;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Uma linha da composição de custo de um serviço, com o preço congelado no momento do cálculo.
 * Assim o orçamento não muda sozinho quando o catálogo de preços é atualizado.
 */
public record LinhaDeCusto(TipoCusto tipo, String codigo, String descricao, Unidade unidade,
                           BigDecimal quantidade, Dinheiro precoUnitario) {

    public LinhaDeCusto {
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(codigo);
        Objects.requireNonNull(descricao);
        Objects.requireNonNull(unidade);
        Objects.requireNonNull(quantidade);
        Objects.requireNonNull(precoUnitario);
    }

    public Dinheiro total() {
        return precoUnitario.multiplicar(quantidade);
    }
}
