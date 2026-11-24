package br.com.orcaobra.dominio;

import br.com.orcaobra.Fixtures;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Percentual;
import br.com.orcaobra.dominio.comum.RegraDeNegocioException;
import br.com.orcaobra.dominio.orcamento.ComposicaoBdi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static br.com.orcaobra.Fixtures.assertValor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComposicaoBdiTest {

    @Test
    @DisplayName("BDI = (1+AC)(1+L)/(1-I) - 1 → 1,1 x 1,1 / 0,9 - 1 = 34,44%")
    void formulaDoTcu() {
        ComposicaoBdi bdi = Fixtures.bdiSimples();
        assertEquals(new BigDecimal("34.44"), bdi.bdi().emPontos());
        assertValor("1344.44", bdi.precoDeVenda(Dinheiro.de(1000)));
    }

    @Test
    @DisplayName("BDI padrão fica na faixa de referência do TCU para edificações (20% a 30%)")
    void bdiPadrao() {
        BigDecimal pontos = ComposicaoBdi.padrao().bdi().emPontos();
        assertTrue(pontos.compareTo(new BigDecimal("20")) > 0 && pontos.compareTo(new BigDecimal("30")) < 0,
                "BDI padrão = " + pontos);
    }

    @Test
    void impostosDe100PorCentoNaoSaoAceitos() {
        assertThrows(RegraDeNegocioException.class, () -> new ComposicaoBdi(Percentual.ZERO, Percentual.ZERO,
                Percentual.ZERO, Percentual.ZERO, Percentual.ZERO, Percentual.de("100")));
    }
}
