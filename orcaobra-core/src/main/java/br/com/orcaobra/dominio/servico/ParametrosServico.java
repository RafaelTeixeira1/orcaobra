package br.com.orcaobra.dominio.servico;

import br.com.orcaobra.dominio.comum.RegraDeNegocioException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Entrada de uma calculadora: a quantidade do serviço (m², m³) e parâmetros extras.
 */
public record ParametrosServico(BigDecimal quantidade, Map<String, BigDecimal> extras) {

    public ParametrosServico {
        Objects.requireNonNull(quantidade, "quantidade é obrigatória");
        if (quantidade.signum() <= 0) {
            throw new RegraDeNegocioException("A quantidade do serviço deve ser maior que zero");
        }
        extras = extras == null ? Map.of() : Map.copyOf(extras);
    }

    public static ParametrosServico de(String quantidade) {
        return new ParametrosServico(new BigDecimal(quantidade), Map.of());
    }

    public BigDecimal valor(String nome, BigDecimal padrao) {
        BigDecimal v = extras.getOrDefault(nome, padrao);
        if (v.signum() < 0) {
            throw new RegraDeNegocioException("O parâmetro '" + nome + "' não pode ser negativo");
        }
        return v;
    }
}
