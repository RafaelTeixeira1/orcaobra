package br.com.orcaobra.dominio.servico.calculadoras;

import br.com.orcaobra.dominio.servico.CalculadoraDeServico;
import br.com.orcaobra.dominio.servico.ComposicaoServico;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static br.com.orcaobra.dominio.servico.CodigosCatalogo.*;

/**
 * Alvenaria de bloco cerâmico 9x19x19 cm, juntas de 1 cm: 25 blocos/m² + 5% de perda
 * e argamassa de assentamento.
 */
public class AlvenariaCalculadora implements CalculadoraDeServico {

    static final BigDecimal BLOCOS_POR_M2 = new BigDecimal("25");
    static final BigDecimal PERDA = new BigDecimal("1.05");
    static final BigDecimal CIMENTO_SC_POR_M2 = new BigDecimal("0.06");
    static final BigDecimal AREIA_M3_POR_M2 = new BigDecimal("0.015");
    static final BigDecimal PEDREIRO_H_POR_M2 = new BigDecimal("0.80");
    static final BigDecimal SERVENTE_H_POR_M2 = new BigDecimal("0.80");

    @Override
    public TipoServico tipo() {
        return TipoServico.ALVENARIA;
    }

    @Override
    public ComposicaoServico calcular(ParametrosServico p) {
        BigDecimal area = p.quantidade();
        return ComposicaoServico.nova()
                .material(BLOCO_CERAMICO, area.multiply(BLOCOS_POR_M2).multiply(PERDA))
                .material(CIMENTO, area.multiply(CIMENTO_SC_POR_M2))
                .material(AREIA, area.multiply(AREIA_M3_POR_M2))
                .horas(PEDREIRO, area.multiply(PEDREIRO_H_POR_M2))
                .horas(SERVENTE, area.multiply(SERVENTE_H_POR_M2))
                .build();
    }
}
