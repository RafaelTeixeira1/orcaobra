package br.com.orcaobra.dominio.servico.calculadoras;

import br.com.orcaobra.dominio.servico.CalculadoraDeServico;
import br.com.orcaobra.dominio.servico.ComposicaoServico;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static br.com.orcaobra.dominio.servico.CodigosCatalogo.*;

/**
 * Contrapiso de argamassa 1:4 (cimento:areia) com espessura configurável e 10% de perda.
 */
public class ContrapisoCalculadora implements CalculadoraDeServico {

    static final BigDecimal PERDA = new BigDecimal("1.10");
    static final BigDecimal CIMENTO_SC_POR_M3 = new BigDecimal("7.0");
    static final BigDecimal AREIA_M3_POR_M3 = new BigDecimal("1.15");
    static final BigDecimal PEDREIRO_H_POR_M2 = new BigDecimal("0.35");
    static final BigDecimal SERVENTE_H_POR_M2 = new BigDecimal("0.45");

    @Override
    public TipoServico tipo() {
        return TipoServico.CONTRAPISO;
    }

    @Override
    public ComposicaoServico calcular(ParametrosServico p) {
        BigDecimal area = p.quantidade();
        BigDecimal espessuraM = p.valor("espessuraCm", new BigDecimal("5"))
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal volume = area.multiply(espessuraM).multiply(PERDA);
        return ComposicaoServico.nova()
                .material(CIMENTO, volume.multiply(CIMENTO_SC_POR_M3))
                .material(AREIA, volume.multiply(AREIA_M3_POR_M3))
                .horas(PEDREIRO, area.multiply(PEDREIRO_H_POR_M2))
                .horas(SERVENTE, area.multiply(SERVENTE_H_POR_M2))
                .build();
    }
}
