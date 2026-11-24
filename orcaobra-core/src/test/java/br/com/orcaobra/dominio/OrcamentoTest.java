package br.com.orcaobra.dominio;

import br.com.orcaobra.Fixtures;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.RegraDeNegocioException;
import br.com.orcaobra.dominio.orcamento.AnaliseDeMargem;
import br.com.orcaobra.dominio.orcamento.CustoRealizado;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import br.com.orcaobra.dominio.orcamento.StatusOrcamento;
import br.com.orcaobra.dominio.orcamento.TipoCusto;
import br.com.orcaobra.dominio.orcamento.eventos.EventoDeDominio;
import br.com.orcaobra.dominio.orcamento.eventos.MargemAbaixoDoMinimo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static br.com.orcaobra.Fixtures.assertValor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrcamentoTest {

    private static final Instant AGORA = Instant.parse("2026-11-24T12:00:00Z");

    private static Orcamento emExecucao() {
        Orcamento o = Fixtures.orcamentoVazio();
        o.adicionarItem(Fixtures.itemDeMil());
        o.alterarStatus(StatusOrcamento.ENVIADO, AGORA);
        o.alterarStatus(StatusOrcamento.APROVADO, AGORA);
        o.alterarStatus(StatusOrcamento.EM_EXECUCAO, AGORA);
        o.retirarEventos();
        return o;
    }

    private static CustoRealizado custo(TipoCusto tipo, String valor) {
        return new CustoRealizado(UUID.randomUUID(), LocalDate.of(2026, 11, 24), tipo, "nota", Dinheiro.de(valor));
    }

    @Test
    @DisplayName("Preço, impostos, indiretos e margem prevista batem com a conta manual")
    void analiseDeMargemPrevista() {
        Orcamento o = Fixtures.orcamentoVazio();
        o.adicionarItem(Fixtures.itemDeMil());

        AnaliseDeMargem a = o.analisarMargem();

        assertValor("1000", a.custoDiretoPrevisto());
        assertValor("1344.44", a.precoDeVenda());
        assertValor("134.44", a.impostos());
        assertValor("100", a.custosIndiretos());
        assertValor("110", a.lucroPrevisto());          // 1344,44 - 134,44 - 100 - 1000
        assertEquals(new BigDecimal("8.18"), a.margemPrevista().emPontos());
        assertFalse(a.abaixoDaMargemMinima());
    }

    @Test
    @DisplayName("Estouro de material reduz a margem projetada e dispara o alerta uma única vez")
    void estouroDeCustoDisparaAlerta() {
        Orcamento o = emExecucao();

        o.lancarCusto(custo(TipoCusto.MATERIAL, "500"), AGORA);   // dentro do previsto (600)
        assertTrue(o.retirarEventos().isEmpty());

        o.lancarCusto(custo(TipoCusto.MATERIAL, "300"), AGORA);   // 800 > 600 previsto
        AnaliseDeMargem a = o.analisarMargem();
        assertValor("1200", a.custoDiretoProjetado());            // 800 material + 400 mão de obra
        assertValor("-90", a.lucroProjetado());
        assertTrue(a.abaixoDaMargemMinima());

        List<EventoDeDominio> eventos = o.retirarEventos();
        assertEquals(1, eventos.size());
        assertInstanceOf(MargemAbaixoDoMinimo.class, eventos.get(0));

        o.lancarCusto(custo(TipoCusto.OUTROS, "10"), AGORA);      // continua abaixo: sem novo alerta
        assertTrue(o.retirarEventos().isEmpty());
    }

    @Test
    void naoEditaItensForaDoRascunho() {
        Orcamento o = emExecucao();
        assertThrows(RegraDeNegocioException.class, () -> o.adicionarItem(Fixtures.itemDeMil()));
    }

    @Test
    void naoLancaCustoAntesDaExecucao() {
        Orcamento o = Fixtures.orcamentoVazio();
        assertThrows(RegraDeNegocioException.class, () -> o.lancarCusto(custo(TipoCusto.MATERIAL, "1"), AGORA));
    }

    @Test
    @DisplayName("Transições inválidas de status são bloqueadas (padrão State)")
    void transicoesDeStatus() {
        Orcamento o = Fixtures.orcamentoVazio();
        assertThrows(RegraDeNegocioException.class, () -> o.alterarStatus(StatusOrcamento.ENVIADO, AGORA),
                "sem itens não pode enviar");
        o.adicionarItem(Fixtures.itemDeMil());
        assertThrows(RegraDeNegocioException.class, () -> o.alterarStatus(StatusOrcamento.CONCLUIDO, AGORA));
        o.alterarStatus(StatusOrcamento.ENVIADO, AGORA);
        o.alterarStatus(StatusOrcamento.RECUSADO, AGORA);
        o.alterarStatus(StatusOrcamento.RASCUNHO, AGORA);
        assertEquals(StatusOrcamento.RASCUNHO, o.status());
        assertEquals(3, o.retirarEventos().size());
    }

    @Test
    @DisplayName("Lista de materiais soma serviços e arredonda unidades discretas para cima")
    void listaDeMateriais() {
        Orcamento o = Fixtures.orcamentoVazio();
        o.adicionarItem(Fixtures.itemDeMil());
        o.adicionarItem(Fixtures.itemDeMil());
        var lista = o.listaDeMateriais();
        assertEquals(1, lista.size());
        assertEquals(0, new BigDecimal("1000").compareTo(lista.get(0).quantidadeCompra()));
        assertValor("1200", lista.get(0).totalCompra());
    }
}
