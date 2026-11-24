package br.com.orcaobra.dominio.servico.calculadoras;

import br.com.orcaobra.dominio.servico.CalculadoraDeServico;
import br.com.orcaobra.dominio.servico.ComposicaoServico;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static br.com.orcaobra.dominio.servico.CodigosCatalogo.*;

/**
 * Pintura acrílica: 1 demão de selador (12 m²/L) + N demãos de tinta (10 m²/L por demão).
 */
public class PinturaCalculadora implements CalculadoraDeServico {

    static final BigDecimal RENDIMENTO_SELADOR_M2_POR_L = new BigDecimal("12");
    static final BigDecimal RENDIMENTO_TINTA_M2_POR_L = new BigDecimal("10");
    static final BigDecimal PINTOR_H_POR_M2_SELADOR = new BigDecimal("0.08");
    static final BigDecimal PINTOR_H_POR_M2_DEMAO = new BigDecimal("0.15");

    @Override
    public TipoServico tipo() {
        return TipoServico.PINTURA;
    }

    @Override
    public ComposicaoServico calcular(ParametrosServico p) {
        BigDecimal area = p.quantidade();
        BigDecimal demaos = p.valor("demaos", BigDecimal.valueOf(2));
        return ComposicaoServico.nova()
                .material(SELADOR, area.divide(RENDIMENTO_SELADOR_M2_POR_L, 6, RoundingMode.HALF_UP))
                .material(TINTA_ACRILICA, area.multiply(demaos)
                        .divide(RENDIMENTO_TINTA_M2_POR_L, 6, RoundingMode.HALF_UP))
                .horas(PINTOR, area.multiply(PINTOR_H_POR_M2_SELADOR)
                        .add(area.multiply(demaos).multiply(PINTOR_H_POR_M2_DEMAO)))
                .build();
    }
}
