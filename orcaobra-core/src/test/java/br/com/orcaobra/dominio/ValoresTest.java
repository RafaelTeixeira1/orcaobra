package br.com.orcaobra.dominio;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Percentual;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static br.com.orcaobra.Fixtures.assertValor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValoresTest {

    @Test
    @DisplayName("Dinheiro sempre arredonda para 2 casas (arredondamento bancário)")
    void arredondamento() {
        assertValor("10.12", Dinheiro.de("10.125"));
        assertValor("10.14", Dinheiro.de("10.135"));
    }

    @Test
    void operacoes() {
        Dinheiro cem = Dinheiro.de(100);
        assertValor("150.00", cem.somar(Dinheiro.de("50")));
        assertValor("25.00", cem.multiplicar(Percentual.de("25")));
        assertEquals(new BigDecimal("50.00"), Dinheiro.de(50).proporcaoDe(cem).emPontos());
        assertEquals(Percentual.ZERO, cem.proporcaoDe(Dinheiro.ZERO));
        assertTrue(Dinheiro.de("-1").ehNegativo());
    }

    @Test
    void percentualEmPontos() {
        assertEquals(new BigDecimal("0.074000"), Percentual.de("7.4").fracao());
        assertEquals("7,40%", Percentual.de("7.4").toString());
    }
}
