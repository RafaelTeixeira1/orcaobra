package br.com.orcaobra.dominio;

import br.com.orcaobra.dominio.servico.ComposicaoServico;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.RegistroDeCalculadoras;
import br.com.orcaobra.dominio.servico.TipoServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static br.com.orcaobra.Fixtures.assertQtd;
import static br.com.orcaobra.dominio.servico.CodigosCatalogo.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadorasTest {

    private final RegistroDeCalculadoras registro = RegistroDeCalculadoras.padrao();

    private static BigDecimal qtd(List<ComposicaoServico.Consumo> consumos, String codigo) {
        return consumos.stream().filter(c -> c.codigo().equals(codigo)).findFirst().orElseThrow().quantidade();
    }

    @Test
    @DisplayName("Existe uma calculadora para cada tipo de serviço")
    void todosOsServicosTemCalculadora() {
        for (TipoServico tipo : TipoServico.values()) {
            ComposicaoServico c = registro.para(tipo).calcular(ParametrosServico.de("1"));
            assertFalse(c.materiais().isEmpty(), tipo + " sem materiais");
            assertFalse(c.maoDeObra().isEmpty(), tipo + " sem mão de obra");
        }
    }

    @Test
    @DisplayName("Alvenaria: 10 m² → 262,5 blocos (25/m² + 5% de perda)")
    void alvenaria() {
        ComposicaoServico c = registro.para(TipoServico.ALVENARIA).calcular(ParametrosServico.de("10"));
        assertQtd("262.5", qtd(c.materiais(), BLOCO_CERAMICO));
        assertQtd("8", qtd(c.maoDeObra(), PEDREIRO));
    }

    @Test
    @DisplayName("Concreto: 2 m³ → 14,7 sacos de cimento (7 sc/m³ + 5%)")
    void concreto() {
        ComposicaoServico c = registro.para(TipoServico.CONCRETO_ESTRUTURAL).calcular(ParametrosServico.de("2"));
        assertQtd("14.7", qtd(c.materiais(), CIMENTO));
        assertQtd("1.764", qtd(c.materiais(), BRITA));
    }

    @Test
    @DisplayName("Contrapiso respeita a espessura informada")
    void contrapisoComEspessura() {
        var p = new ParametrosServico(new BigDecimal("100"), Map.of("espessuraCm", new BigDecimal("3")));
        ComposicaoServico c = registro.para(TipoServico.CONTRAPISO).calcular(p);
        // 100 m² x 0,03 m x 1,10 = 3,3 m³ de argamassa → 3,3 x 7 = 23,1 sacos
        assertQtd("23.1", qtd(c.materiais(), CIMENTO));
    }

    @Test
    @DisplayName("Pintura: 100 m² com 3 demãos → 30 L de tinta")
    void pintura() {
        var p = new ParametrosServico(new BigDecimal("100"), Map.of("demaos", new BigDecimal("3")));
        ComposicaoServico c = registro.para(TipoServico.PINTURA).calcular(p);
        assertQtd("30", qtd(c.materiais(), TINTA_ACRILICA));
        assertQtd("53", qtd(c.maoDeObra(), PINTOR));
    }

    @Test
    void pisoUsaPerdaPadraoDe10PorCento() {
        ComposicaoServico c = registro.para(TipoServico.PISO_CERAMICO).calcular(ParametrosServico.de("50"));
        assertQtd("55", qtd(c.materiais(), PISO_CERAMICO));
        assertEquals(3, c.materiais().size());
    }

    @Test
    void quantidadeZeroNaoEhPermitida() {
        assertThrows(RuntimeException.class, () -> ParametrosServico.de("0"));
    }
}
