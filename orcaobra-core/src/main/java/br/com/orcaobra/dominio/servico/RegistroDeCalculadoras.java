package br.com.orcaobra.dominio.servico;

import br.com.orcaobra.dominio.comum.RegraDeNegocioException;
import br.com.orcaobra.dominio.servico.calculadoras.AlvenariaCalculadora;
import br.com.orcaobra.dominio.servico.calculadoras.ConcretoCalculadora;
import br.com.orcaobra.dominio.servico.calculadoras.ContrapisoCalculadora;
import br.com.orcaobra.dominio.servico.calculadoras.PinturaCalculadora;
import br.com.orcaobra.dominio.servico.calculadoras.PisoCeramicoCalculadora;
import br.com.orcaobra.dominio.servico.calculadoras.RebocoCalculadora;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory/Registry: devolve a estratégia de cálculo certa para cada {@link TipoServico}.
 */
public final class RegistroDeCalculadoras {

    private final Map<TipoServico, CalculadoraDeServico> calculadoras = new EnumMap<>(TipoServico.class);

    public RegistroDeCalculadoras(Collection<? extends CalculadoraDeServico> estrategias) {
        for (CalculadoraDeServico c : estrategias) {
            if (calculadoras.putIfAbsent(c.tipo(), c) != null) {
                throw new IllegalStateException("Mais de uma calculadora para " + c.tipo());
            }
        }
    }

    /** Registro com todas as calculadoras que vêm com o sistema. */
    public static RegistroDeCalculadoras padrao() {
        return new RegistroDeCalculadoras(List.of(
                new ConcretoCalculadora(),
                new AlvenariaCalculadora(),
                new RebocoCalculadora(),
                new ContrapisoCalculadora(),
                new PisoCeramicoCalculadora(),
                new PinturaCalculadora()));
    }

    public CalculadoraDeServico para(TipoServico tipo) {
        CalculadoraDeServico c = calculadoras.get(tipo);
        if (c == null) {
            throw new RegraDeNegocioException("Não há calculadora para o serviço " + tipo);
        }
        return c;
    }
}
