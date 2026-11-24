package br.com.orcaobra.dominio.servico.calculadoras;

import br.com.orcaobra.dominio.servico.CalculadoraDeServico;
import br.com.orcaobra.dominio.servico.ComposicaoServico;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static br.com.orcaobra.dominio.servico.CodigosCatalogo.*;

/**
 * Concreto traço 1:2:3 (cimento:areia:brita) virado em betoneira, fck aprox. 20 MPa.
 * Consumos por m³, já com 5% de perda.
 */
public class ConcretoCalculadora implements CalculadoraDeServico {

    static final BigDecimal PERDA = new BigDecimal("1.05");
    static final BigDecimal CIMENTO_SC_POR_M3 = new BigDecimal("7.0");
    static final BigDecimal AREIA_M3_POR_M3 = new BigDecimal("0.56");
    static final BigDecimal BRITA_M3_POR_M3 = new BigDecimal("0.84");
    static final BigDecimal PEDREIRO_H_POR_M3 = new BigDecimal("2.0");
    static final BigDecimal SERVENTE_H_POR_M3 = new BigDecimal("6.0");

    @Override
    public TipoServico tipo() {
        return TipoServico.CONCRETO_ESTRUTURAL;
    }

    @Override
    public ComposicaoServico calcular(ParametrosServico p) {
        BigDecimal v = p.quantidade();
        return ComposicaoServico.nova()
                .material(CIMENTO, v.multiply(CIMENTO_SC_POR_M3).multiply(PERDA))
                .material(AREIA, v.multiply(AREIA_M3_POR_M3).multiply(PERDA))
                .material(BRITA, v.multiply(BRITA_M3_POR_M3).multiply(PERDA))
                .horas(PEDREIRO, v.multiply(PEDREIRO_H_POR_M3))
                .horas(SERVENTE, v.multiply(SERVENTE_H_POR_M3))
                .build();
    }
}
