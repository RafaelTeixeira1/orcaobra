package br.com.orcaobra.dominio.orcamento;

import br.com.orcaobra.dominio.comum.RegraDeNegocioException;

import java.math.BigDecimal;

/** Dados da obra orçada. A área construída é opcional e serve para calcular o custo por m². */
public record Obra(String descricao, String endereco, String cidade, BigDecimal areaConstruidaM2) {

    public Obra {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraDeNegocioException("A descrição da obra é obrigatória");
        }
        if (areaConstruidaM2 != null && areaConstruidaM2.signum() <= 0) {
            throw new RegraDeNegocioException("A área construída deve ser maior que zero");
        }
    }
}
