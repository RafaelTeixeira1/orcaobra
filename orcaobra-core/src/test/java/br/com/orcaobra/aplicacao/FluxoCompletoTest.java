package br.com.orcaobra.aplicacao;

import br.com.orcaobra.aplicacao.fakes.CatalogoEmMemoria;
import br.com.orcaobra.aplicacao.fakes.OrcamentoRepositoryEmMemoria;
import br.com.orcaobra.aplicacao.fakes.PublicadorQueGuarda;
import br.com.orcaobra.aplicacao.porta.entrada.AcompanharMargemUseCase.LancarCustoComando;
import br.com.orcaobra.aplicacao.porta.entrada.GerarPropostaUseCase;
import br.com.orcaobra.aplicacao.porta.entrada.GerenciarOrcamentoUseCase.AdicionarServicoComando;
import br.com.orcaobra.aplicacao.porta.entrada.GerenciarOrcamentoUseCase.CriarOrcamentoComando;
import br.com.orcaobra.aplicacao.servico.AcompanharMargemService;
import br.com.orcaobra.aplicacao.servico.CatalogoService;
import br.com.orcaobra.aplicacao.servico.GerarPropostaService;
import br.com.orcaobra.aplicacao.servico.GerenciarOrcamentoService;
import br.com.orcaobra.aplicacao.servico.PrecificadorDeServico;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Percentual;
import br.com.orcaobra.dominio.orcamento.AnaliseDeMargem;
import br.com.orcaobra.dominio.orcamento.Cliente;
import br.com.orcaobra.dominio.orcamento.Obra;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import br.com.orcaobra.dominio.orcamento.StatusOrcamento;
import br.com.orcaobra.dominio.orcamento.TipoCusto;
import br.com.orcaobra.dominio.orcamento.eventos.MargemAbaixoDoMinimo;
import br.com.orcaobra.dominio.proposta.DadosEmpresa;
import br.com.orcaobra.dominio.servico.RegistroDeCalculadoras;
import br.com.orcaobra.dominio.servico.TipoServico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import static br.com.orcaobra.Fixtures.assertValor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercita os casos de uso de ponta a ponta usando adaptadores em memória. */
class FluxoCompletoTest {

    private OrcamentoRepositoryEmMemoria repositorio;
    private CatalogoEmMemoria catalogo;
    private PublicadorQueGuarda publicador;
    private GerenciarOrcamentoService gerenciar;
    private AcompanharMargemService margem;
    private final Clock relogio = Clock.fixed(Instant.parse("2026-11-24T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @BeforeEach
    void preparar() {
        repositorio = new OrcamentoRepositoryEmMemoria();
        catalogo = new CatalogoEmMemoria();
        publicador = new PublicadorQueGuarda();
        gerenciar = new GerenciarOrcamentoService(repositorio,
                new PrecificadorDeServico(RegistroDeCalculadoras.padrao(), catalogo), publicador, relogio);
        margem = new AcompanharMargemService(repositorio, publicador, relogio);
    }

    private Orcamento criarComAlvenaria() {
        Orcamento o = gerenciar.criar(new CriarOrcamentoComando(
                new Cliente("João", null, null, null),
                new Obra("Muro", "Rua 1", "Ceres-GO", null),
                null, Percentual.de("5"), 10));
        return gerenciar.adicionarServico(o.id(),
                new AdicionarServicoComando(TipoServico.ALVENARIA, "Muro frontal", new BigDecimal("10"), Map.of()));
    }

    @Test
    @DisplayName("Adicionar serviço calcula materiais e mão de obra com os preços do catálogo")
    void adicionarServicoPrecifica() {
        Orcamento o = criarComAlvenaria();
        // 262,5 blocos x 1,10 + 0,6 sc x 38 + 0,15 m³ x 140 = 288,75 + 22,80 + 21,00
        assertValor("332.55", o.custo(TipoCusto.MATERIAL));
        // 8 h pedreiro x 25 + 8 h servente x 16
        assertValor("328.00", o.custo(TipoCusto.MAO_DE_OBRA));
        assertEquals(java.time.LocalDate.of(2026, 11, 24), o.dataCriacao());
    }

    @Test
    @DisplayName("Mudar o preço no catálogo não altera orçamentos já calculados")
    void precosCongelados() {
        Orcamento o = criarComAlvenaria();
        new CatalogoService(catalogo).atualizarPrecoMaterial("BLOCO_CERAMICO_9X19X19", Dinheiro.de("5"));
        assertValor("332.55", gerenciar.buscar(o.id()).custo(TipoCusto.MATERIAL));
    }

    @Test
    @DisplayName("Ciclo completo: enviar, aprovar, executar, estourar custo e receber alerta")
    void cicloCompleto() {
        Orcamento o = criarComAlvenaria();
        gerenciar.alterarStatus(o.id(), StatusOrcamento.ENVIADO);
        gerenciar.alterarStatus(o.id(), StatusOrcamento.APROVADO);
        gerenciar.alterarStatus(o.id(), StatusOrcamento.EM_EXECUCAO);

        AnaliseDeMargem a = margem.lancarCusto(o.id(),
                new LancarCustoComando(null, TipoCusto.MAO_DE_OBRA, "Empreiteiro", Dinheiro.de("600")));

        assertTrue(a.abaixoDaMargemMinima());
        assertTrue(publicador.publicados.stream().anyMatch(e -> e instanceof MargemAbaixoDoMinimo));
        assertEquals(3 + 1, publicador.publicados.size());
    }

    @Test
    void gerarPropostaUsaOGerador() {
        Orcamento o = criarComAlvenaria();
        var servico = new GerarPropostaService(repositorio, p -> ("PDF " + p.numero()).getBytes(),
                new DadosEmpresa("Empresa", "Eng.", "CREA", "", "", "Ceres-GO"), relogio);

        var doc = servico.gerar(new GerarPropostaUseCase.GerarPropostaComando(o.id(), null, null, null));

        assertTrue(doc.nomeArquivo().startsWith("proposta-ORC-2026-"));
        assertTrue(new String(doc.conteudo()).startsWith("PDF ORC-2026-"));
    }
}
