package br.com.orcaobra.dominio.servico.calculadoras;

import br.com.orcaobra.dominio.servico.CalculadoraDeServico;
import br.com.orcaobra.dominio.servico.ComposicaoServico;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static br.com.orcaobra.dominio.servico.CodigosCatalogo.*;

/**
 * Piso cerâmico assentado com argamassa colante AC-I (5 kg/m², saco de 20 kg) e rejunte.
 */
public class PisoCeramicoCalculadora implements CalculadoraDeServico {

    static final BigDecimal ARGAMASSA_SC_POR_M2 = new BigDecimal("0.25");
    static final BigDecimal REJUNTE_KG_POR_M2 = new BigDecimal("0.30");
    static final BigDecimal PEDREIRO_H_POR_M2 = new BigDecimal("0.60");
    static final BigDecimal SERVENTE_H_POR_M2 = new BigDecimal("0.30");

    @Override
    public TipoServico tipo() {
        return TipoServico.PISO_CERAMICO;
    }

    @Override
    public ComposicaoServico calcular(ParametrosServico p) {
        BigDecimal area = p.quantidade();
        BigDecimal fatorPerda = BigDecimal.ONE.add(p.valor("perdaPercentual", BigDecimal.TEN)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        return ComposicaoServico.nova()
                .material(PISO_CERAMICO, area.multiply(fatorPerda))
                .material(ARGAMASSA_COLANTE, area.multiply(ARGAMASSA_SC_POR_M2))
                .material(REJUNTE, area.multiply(REJUNTE_KG_POR_M2))
                .horas(PEDREIRO, area.multiply(PEDREIRO_H_POR_M2))
                .horas(SERVENTE, area.multiply(SERVENTE_H_POR_M2))
                .build();
    }
}
