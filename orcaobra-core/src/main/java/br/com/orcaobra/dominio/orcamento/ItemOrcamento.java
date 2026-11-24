package br.com.orcaobra.dominio.orcamento;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Um serviço do orçamento (ex.: "Alvenaria da sala - 42 m²") com a composição de custos. */
public record ItemOrcamento(UUID id, TipoServico tipo, String descricao,
                            ParametrosServico parametros, List<LinhaDeCusto> linhas) {

    public ItemOrcamento {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(parametros);
        descricao = (descricao == null || descricao.isBlank()) ? tipo.descricao() : descricao.trim();
        linhas = List.copyOf(linhas);
    }

    public Dinheiro custo(TipoCusto tipoCusto) {
        return linhas.stream()
                .filter(l -> l.tipo() == tipoCusto)
                .map(LinhaDeCusto::total)
                .reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public Dinheiro custoDireto() {
        return linhas.stream().map(LinhaDeCusto::total).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }
}
