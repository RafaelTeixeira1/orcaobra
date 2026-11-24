package br.com.orcaobra;

import br.com.orcaobra.dominio.catalogo.FuncaoMaoDeObra;
import br.com.orcaobra.dominio.catalogo.Material;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Percentual;
import br.com.orcaobra.dominio.comum.Unidade;
import br.com.orcaobra.dominio.orcamento.Cliente;
import br.com.orcaobra.dominio.orcamento.ComposicaoBdi;
import br.com.orcaobra.dominio.orcamento.ItemOrcamento;
import br.com.orcaobra.dominio.orcamento.LinhaDeCusto;
import br.com.orcaobra.dominio.orcamento.Obra;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import br.com.orcaobra.dominio.orcamento.TipoCusto;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static br.com.orcaobra.dominio.servico.CodigosCatalogo.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Dados de teste reutilizáveis. */
public final class Fixtures {

    private Fixtures() {
    }

    public static void assertValor(String esperado, Dinheiro atual) {
        assertEquals(new BigDecimal(esperado).setScale(2), atual.valor());
    }

    public static void assertQtd(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual),
                "esperado " + esperado + " mas foi " + atual.toPlainString());
    }

    /** BDI simples para conferir contas de cabeça: AC 10%, lucro 10%, impostos 10%. */
    public static ComposicaoBdi bdiSimples() {
        return new ComposicaoBdi(Percentual.de("10"), Percentual.ZERO, Percentual.ZERO,
                Percentual.ZERO, Percentual.de("10"), Percentual.de("10"));
    }

    public static Orcamento orcamentoVazio() {
        return Orcamento.builder()
                .cliente(new Cliente("Maria Souza", "123.456.789-00", "maria@email.com", "(62) 99999-0000"))
                .obra(new Obra("Casa térrea 2 quartos", "Rua 10, Qd 5", "Ceres-GO", new BigDecimal("70")))
                .bdi(bdiSimples())
                .margemMinima(Percentual.de("5"))
                .dataCriacao(LocalDate.of(2026, 11, 24))
                .build();
    }

    /** Item com R$ 600 de material e R$ 400 de mão de obra (custo direto R$ 1.000). */
    public static ItemOrcamento itemDeMil() {
        return new ItemOrcamento(UUID.randomUUID(), TipoServico.ALVENARIA, "Paredes", ParametrosServico.de("10"),
                List.of(new LinhaDeCusto(TipoCusto.MATERIAL, BLOCO_CERAMICO, "Bloco", Unidade.UN,
                                new BigDecimal("500"), Dinheiro.de("1.20")),
                        new LinhaDeCusto(TipoCusto.MAO_DE_OBRA, PEDREIRO, "Pedreiro", Unidade.H,
                                new BigDecimal("16"), Dinheiro.de("25"))));
    }

    public static List<Material> materiais() {
        return List.of(
                new Material(CIMENTO, "Cimento CP II 50 kg", Unidade.SC, Dinheiro.de("38.00")),
                new Material(AREIA, "Areia média", Unidade.M3, Dinheiro.de("140.00")),
                new Material(BRITA, "Brita 1", Unidade.M3, Dinheiro.de("150.00")),
                new Material(BLOCO_CERAMICO, "Bloco cerâmico 9x19x19", Unidade.UN, Dinheiro.de("1.10")),
                new Material(ARGAMASSA_COLANTE, "Argamassa colante AC-I 20 kg", Unidade.SC, Dinheiro.de("22.00")),
                new Material(REJUNTE, "Rejunte", Unidade.KG, Dinheiro.de("8.00")),
                new Material(PISO_CERAMICO, "Piso cerâmico", Unidade.M2, Dinheiro.de("45.00")),
                new Material(TINTA_ACRILICA, "Tinta acrílica", Unidade.L, Dinheiro.de("22.00")),
                new Material(SELADOR, "Selador acrílico", Unidade.L, Dinheiro.de("12.00")));
    }

    public static List<FuncaoMaoDeObra> funcoes() {
        return List.of(
                new FuncaoMaoDeObra(PEDREIRO, "Pedreiro", Dinheiro.de("25.00")),
                new FuncaoMaoDeObra(SERVENTE, "Servente", Dinheiro.de("16.00")),
                new FuncaoMaoDeObra(PINTOR, "Pintor", Dinheiro.de("25.00")));
    }
}
