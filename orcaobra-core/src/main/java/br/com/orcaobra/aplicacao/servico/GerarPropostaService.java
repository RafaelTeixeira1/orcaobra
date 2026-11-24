package br.com.orcaobra.aplicacao.servico;

import br.com.orcaobra.aplicacao.porta.entrada.GerarPropostaUseCase;
import br.com.orcaobra.aplicacao.porta.saida.GeradorDeDocumentoProposta;
import br.com.orcaobra.aplicacao.porta.saida.OrcamentoRepository;
import br.com.orcaobra.dominio.comum.RecursoNaoEncontradoException;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import br.com.orcaobra.dominio.proposta.DadosEmpresa;
import br.com.orcaobra.dominio.proposta.MontadorDeProposta;
import br.com.orcaobra.dominio.proposta.Proposta;

import java.time.Clock;
import java.time.LocalDate;

public class GerarPropostaService implements GerarPropostaUseCase {

    private final OrcamentoRepository repositorio;
    private final GeradorDeDocumentoProposta gerador;
    private final DadosEmpresa empresa;
    private final Clock relogio;
    private final MontadorDeProposta montador = new MontadorDeProposta();

    public GerarPropostaService(OrcamentoRepository repositorio, GeradorDeDocumentoProposta gerador,
                                DadosEmpresa empresa, Clock relogio) {
        this.repositorio = repositorio;
        this.gerador = gerador;
        this.empresa = empresa;
        this.relogio = relogio;
    }

    @Override
    public DocumentoGerado gerar(GerarPropostaComando cmd) {
        Orcamento orcamento = repositorio.buscarPorId(cmd.orcamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento " + cmd.orcamentoId() + " não encontrado"));
        Proposta proposta = montador.montar(orcamento, empresa, LocalDate.now(relogio),
                textoOuPadrao(cmd.condicoesPagamento(), "30% na assinatura do contrato e o restante conforme medições."),
                textoOuPadrao(cmd.prazoExecucao(), "A combinar após aprovação."),
                cmd.observacoes());
        return new DocumentoGerado("proposta-" + proposta.numero() + ".pdf", gerador.gerar(proposta));
    }

    private static String textoOuPadrao(String texto, String padrao) {
        return texto == null || texto.isBlank() ? padrao : texto;
    }
}
