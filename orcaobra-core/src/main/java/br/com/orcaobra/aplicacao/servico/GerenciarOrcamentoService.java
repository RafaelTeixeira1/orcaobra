package br.com.orcaobra.aplicacao.servico;

import br.com.orcaobra.aplicacao.porta.entrada.GerenciarOrcamentoUseCase;
import br.com.orcaobra.aplicacao.porta.saida.OrcamentoRepository;
import br.com.orcaobra.aplicacao.porta.saida.PublicadorDeEventos;
import br.com.orcaobra.dominio.comum.RecursoNaoEncontradoException;
import br.com.orcaobra.dominio.orcamento.ComposicaoBdi;
import br.com.orcaobra.dominio.orcamento.ItemOrcamento;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import br.com.orcaobra.dominio.orcamento.StatusOrcamento;
import br.com.orcaobra.dominio.servico.ParametrosServico;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GerenciarOrcamentoService implements GerenciarOrcamentoUseCase {

    private final OrcamentoRepository repositorio;
    private final PrecificadorDeServico precificador;
    private final PublicadorDeEventos publicador;
    private final Clock relogio;

    public GerenciarOrcamentoService(OrcamentoRepository repositorio, PrecificadorDeServico precificador,
                                     PublicadorDeEventos publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.precificador = precificador;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    @Override
    public Orcamento criar(CriarOrcamentoComando cmd) {
        Orcamento.Builder builder = Orcamento.builder()
                .cliente(cmd.cliente())
                .obra(cmd.obra())
                .dataCriacao(LocalDate.now(relogio))
                .bdi(cmd.bdi() != null ? cmd.bdi() : ComposicaoBdi.padrao());
        if (cmd.margemMinima() != null) {
            builder.margemMinima(cmd.margemMinima());
        }
        if (cmd.validadeDias() != null) {
            builder.validadeDias(cmd.validadeDias());
        }
        return repositorio.salvar(builder.build());
    }

    @Override
    public Orcamento adicionarServico(UUID orcamentoId, AdicionarServicoComando cmd) {
        Orcamento orcamento = buscar(orcamentoId);
        ParametrosServico parametros = new ParametrosServico(cmd.quantidade(), cmd.parametros());
        var linhas = precificador.precificar(cmd.tipo(), parametros);
        orcamento.adicionarItem(new ItemOrcamento(UUID.randomUUID(), cmd.tipo(), cmd.descricao(), parametros, linhas));
        return salvarEPublicar(orcamento);
    }

    @Override
    public Orcamento removerServico(UUID orcamentoId, UUID itemId) {
        Orcamento orcamento = buscar(orcamentoId);
        orcamento.removerItem(itemId);
        return salvarEPublicar(orcamento);
    }

    @Override
    public Orcamento definirCondicoes(UUID orcamentoId, DefinirCondicoesComando cmd) {
        Orcamento orcamento = buscar(orcamentoId);
        orcamento.definirCondicoes(cmd.bdi(), cmd.margemMinima(), cmd.validadeDias());
        return salvarEPublicar(orcamento);
    }

    @Override
    public Orcamento alterarStatus(UUID orcamentoId, StatusOrcamento novoStatus) {
        Orcamento orcamento = buscar(orcamentoId);
        orcamento.alterarStatus(novoStatus, relogio.instant());
        return salvarEPublicar(orcamento);
    }

    @Override
    public Orcamento buscar(UUID orcamentoId) {
        return repositorio.buscarPorId(orcamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento " + orcamentoId + " não encontrado"));
    }

    @Override
    public List<Orcamento> listar() {
        return repositorio.listar();
    }

    private Orcamento salvarEPublicar(Orcamento orcamento) {
        var eventos = orcamento.retirarEventos();
        Orcamento salvo = repositorio.salvar(orcamento);
        publicador.publicar(eventos);
        return salvo;
    }
}
