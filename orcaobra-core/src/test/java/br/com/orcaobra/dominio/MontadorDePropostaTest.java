package br.com.orcaobra.dominio;

import br.com.orcaobra.Fixtures;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.RegraDeNegocioException;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import br.com.orcaobra.dominio.proposta.DadosEmpresa;
import br.com.orcaobra.dominio.proposta.MontadorDeProposta;
import br.com.orcaobra.dominio.proposta.Proposta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static br.com.orcaobra.Fixtures.assertValor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MontadorDePropostaTest {

    private final DadosEmpresa empresa = new DadosEmpresa("Construtora X", "Eng. Fulano", "CREA-GO 0000",
            "(62) 0000-0000", "contato@x.com", "Ceres-GO");
    private final LocalDate hoje = LocalDate.of(2026, 11, 24);

    @Test
    void somaDosItensBateComOTotal() {
        Orcamento o = Fixtures.orcamentoVazio();
        for (int i = 0; i < 3; i++) {
            o.adicionarItem(Fixtures.itemDeMil());
        }
        Proposta p = new MontadorDeProposta().montar(o, empresa, hoje, "à vista", "30 dias", null);

        Dinheiro soma = p.itens().stream().map(Proposta.Item::valor).reduce(Dinheiro.ZERO, Dinheiro::somar);
        assertEquals(p.valorTotal(), soma);
        assertValor("4033.33", p.valorTotal());
        assertValor("57.62", p.valorPorM2());            // 4033,33 / 70 m²
        assertEquals(hoje.plusDays(15), p.validaAte());
        assertTrue(p.numero().startsWith("ORC-2026-"));
    }

    @Test
    void naoGeraPropostaSemItens() {
        assertThrows(RegraDeNegocioException.class, () ->
                new MontadorDeProposta().montar(Fixtures.orcamentoVazio(), empresa, hoje, "", "", ""));
    }
}
