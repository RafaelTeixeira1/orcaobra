package br.com.orcaobra.aplicacao.porta.saida;

import br.com.orcaobra.dominio.orcamento.Orcamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de saída: onde os orçamentos são guardados (JPA, memória...). */
public interface OrcamentoRepository {

    Orcamento salvar(Orcamento orcamento);

    Optional<Orcamento> buscarPorId(UUID id);

    List<Orcamento> listar();
}
