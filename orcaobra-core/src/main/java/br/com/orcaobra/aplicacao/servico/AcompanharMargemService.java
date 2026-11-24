package br.com.orcaobra.aplicacao.servico;

import br.com.orcaobra.aplicacao.porta.entrada.AcompanharMargemUseCase;
import br.com.orcaobra.aplicacao.porta.saida.OrcamentoRepository;
import br.com.orcaobra.aplicacao.porta.saida.PublicadorDeEventos;
import br.com.orcaobra.dominio.comum.RecursoNaoEncontradoException;
import br.com.orcaobra.dominio.orcamento.AnaliseDeMargem;
import br.com.orcaobra.dominio.orcamento.CustoRealizado;
import br.com.orcaobra.dominio.orcamento.Orcamento;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

public class AcompanharMargemService implements AcompanharMargemUseCase {

    private final OrcamentoRepository repositorio;
    private final PublicadorDeEventos publicador;
    private final Clock relogio;

    public AcompanharMargemService(OrcamentoRepository repositorio, PublicadorDeEventos publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    @Override
    public AnaliseDeMargem lancarCusto(UUID orcamentoId, LancarCustoComando cmd) {
        Orcamento orcamento = carregar(orcamentoId);
        LocalDate data = cmd.data() != null ? cmd.data() : LocalDate.now(relogio);
        orcamento.lancarCusto(new CustoRealizado(UUID.randomUUID(), data, cmd.categoria(), cmd.descricao(), cmd.valor()),
                relogio.instant());
        var eventos = orcamento.retirarEventos();
        repositorio.salvar(orcamento);
        publicador.publicar(eventos);
        return orcamento.analisarMargem();
    }

    @Override
    public AnaliseDeMargem analisar(UUID orcamentoId) {
        return carregar(orcamentoId).analisarMargem();
    }

    private Orcamento carregar(UUID id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento " + id + " não encontrado"));
    }
}
