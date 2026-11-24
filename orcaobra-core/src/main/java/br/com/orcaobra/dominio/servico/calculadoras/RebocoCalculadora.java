package br.com.orcaobra.dominio.servico.calculadoras;

import br.com.orcaobra.dominio.servico.CalculadoraDeServico;
import br.com.orcaobra.dominio.servico.ComposicaoServico;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static br.com.orcaobra.dominio.servico.CodigosCatalogo.*;

/**
 * Chapisco 1:3 + reboco (massa única) com espessura configurável.
 * Volume de argamassa = área x espessura + 10% de perda.
 */
public class RebocoCalculadora implements CalculadoraDeServico {

    static final BigDecimal PERDA = new BigDecimal("1.10");
    static final BigDecimal CIMENTO_SC_POR_M3_ARGAMASSA = new BigDecimal("3.6");
    static final BigDecimal AREIA_M3_POR_M3_ARGAMASSA = new BigDecimal("1.10");
    static final BigDecimal CHAPISCO_CIMENTO_SC_POR_M2 = new BigDecimal("0.02");
    static final BigDecimal CHAPISCO_AREIA_M3_POR_M2 = new BigDecimal("0.005");
    static final BigDecimal PEDREIRO_H_POR_M2 = new BigDecimal("0.70");
    static final BigDecimal SERVENTE_H_POR_M2 = new BigDecimal("0.70");

    @Override
    public TipoServico tipo() {
        return TipoServico.REBOCO;
    }

    @Override
    public ComposicaoServico calcular(ParametrosServico p) {
        BigDecimal area = p.quantidade();
        BigDecimal espessuraM = p.valor("espessuraCm", new BigDecimal("2"))
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal volume = area.multiply(espessuraM).multiply(PERDA);
        return ComposicaoServico.nova()
                .material(CIMENTO, volume.multiply(CIMENTO_SC_POR_M3_ARGAMASSA)
                        .add(area.multiply(CHAPISCO_CIMENTO_SC_POR_M2)))
                .material(AREIA, volume.multiply(AREIA_M3_POR_M3_ARGAMASSA)
                        .add(area.multiply(CHAPISCO_AREIA_M3_POR_M2)))
                .horas(PEDREIRO, area.multiply(PEDREIRO_H_POR_M2))
                .horas(SERVENTE, area.multiply(SERVENTE_H_POR_M2))
                .build();
    }
}
