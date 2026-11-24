package br.com.orcaobra.aplicacao.porta.entrada;

import br.com.orcaobra.dominio.comum.Percentual;
import br.com.orcaobra.dominio.orcamento.Cliente;
import br.com.orcaobra.dominio.orcamento.ComposicaoBdi;
import br.com.orcaobra.dominio.orcamento.Obra;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import br.com.orcaobra.dominio.orcamento.StatusOrcamento;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Porta de entrada: casos de uso de criação e edição de orçamentos. */
public interface GerenciarOrcamentoUseCase {

    record CriarOrcamentoComando(Cliente cliente, Obra obra, ComposicaoBdi bdi,
                                 Percentual margemMinima, Integer validadeDias) {
    }

    record AdicionarServicoComando(TipoServico tipo, String descricao, BigDecimal quantidade,
                                   Map<String, BigDecimal> parametros) {
    }

    record DefinirCondicoesComando(ComposicaoBdi bdi, Percentual margemMinima, int validadeDias) {
    }

    Orcamento criar(CriarOrcamentoComando comando);

    Orcamento adicionarServico(UUID orcamentoId, AdicionarServicoComando comando);

    Orcamento removerServico(UUID orcamentoId, UUID itemId);

    Orcamento definirCondicoes(UUID orcamentoId, DefinirCondicoesComando comando);

    Orcamento alterarStatus(UUID orcamentoId, StatusOrcamento novoStatus);

    Orcamento buscar(UUID orcamentoId);

    List<Orcamento> listar();
}
