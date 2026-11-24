package br.com.orcaobra.api;

import br.com.orcaobra.api.adaptador.saida.pdf.PropostaPdfPadrao;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Unidade;
import br.com.orcaobra.dominio.orcamento.Cliente;
import br.com.orcaobra.dominio.orcamento.Obra;
import br.com.orcaobra.dominio.proposta.DadosEmpresa;
import br.com.orcaobra.dominio.proposta.Proposta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PropostaPdfPadraoTest {

    @Test
    void geraUmPdfValido() {
        Proposta p = new Proposta("ORC-2026-ABCDEF12", LocalDate.of(2026, 11, 24), LocalDate.of(2026, 12, 9),
                new DadosEmpresa("Engenharia Exemplo", "Eng. Civil Fulano", "CREA-GO 1234/D", "(62) 0000-0000",
                        "contato@exemplo.com", "Ceres-GO"),
                new Cliente("Maria Souza", "000.000.000-00", "maria@email.com", "(62) 99999-0000"),
                new Obra("Casa térrea", "Rua 10", "Ceres-GO", new BigDecimal("70")),
                List.of(new Proposta.Item(1, "Alvenaria", new BigDecimal("120"), Unidade.M2, Dinheiro.de("9800.00")),
                        new Proposta.Item(2, "Pintura", new BigDecimal("300"), Unidade.M2, Dinheiro.de("7200.00"))),
                Dinheiro.de("17000.00"), Dinheiro.de("242.86"), "À vista", "60 dias", "Não inclui projetos");

        byte[] pdf = new PropostaPdfPadrao().gerar(p);

        assertThat(pdf.length).isGreaterThan(1000);
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }
}
