package br.com.orcaobra.aplicacao.porta.entrada;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.orcamento.AnaliseDeMargem;
import br.com.orcaobra.dominio.orcamento.TipoCusto;

import java.time.LocalDate;
import java.util.UUID;

/** Porta de entrada: acompanhamento da margem de lucro durante a obra. */
public interface AcompanharMargemUseCase {

    record LancarCustoComando(LocalDate data, TipoCusto categoria, String descricao, Dinheiro valor) {
    }

    AnaliseDeMargem lancarCusto(UUID orcamentoId, LancarCustoComando comando);

    AnaliseDeMargem analisar(UUID orcamentoId);
}
