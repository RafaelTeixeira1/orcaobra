package br.com.orcaobra.dominio.orcamento;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.RegraDeNegocioException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Gasto real lançado durante a execução da obra (nota de material, pagamento de equipe...). */
public record CustoRealizado(UUID id, LocalDate data, TipoCusto categoria, String descricao, Dinheiro valor) {

    public CustoRealizado {
        Objects.requireNonNull(id);
        Objects.requireNonNull(data, "a data é obrigatória");
        Objects.requireNonNull(categoria, "a categoria é obrigatória");
        Objects.requireNonNull(valor, "o valor é obrigatório");
        if (!valor.maiorQue(Dinheiro.ZERO)) {
            throw new RegraDeNegocioException("O valor do custo deve ser maior que zero");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new RegraDeNegocioException("A descrição do custo é obrigatória");
        }
    }
}
