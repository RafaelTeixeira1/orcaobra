package br.com.orcaobra.dominio.orcamento;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Percentual;
import br.com.orcaobra.dominio.comum.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Composição do BDI (Benefícios e Despesas Indiretas), na fórmula usada pelo TCU (Acórdão 2622/2013):
 * <pre>
 * BDI = ((1 + AC + S + R) × (1 + DF) × (1 + L)) / (1 − I) − 1
 * </pre>
 * AC = administração central, S = seguro e garantia, R = risco, DF = despesas financeiras,
 * L = lucro e I = impostos sobre o faturamento.
 */
public record ComposicaoBdi(Percentual administracaoCentral, Percentual seguroEGarantia, Percentual risco,
                            Percentual despesasFinanceiras, Percentual lucro, Percentual impostos) {

    private static final MathContext MC = MathContext.DECIMAL64;

    public ComposicaoBdi {
        Stream.of(administracaoCentral, seguroEGarantia, risco, despesasFinanceiras, lucro, impostos)
                .forEach(p -> {
                    Objects.requireNonNull(p, "todos os componentes do BDI são obrigatórios");
                    if (p.fracao().signum() < 0) {
                        throw new RegraDeNegocioException("Componentes do BDI não podem ser negativos");
                    }
                });
        if (impostos.fracao().compareTo(BigDecimal.ONE) >= 0) {
            throw new RegraDeNegocioException("Impostos devem ser menores que 100%");
        }
    }

    /** Valores de referência para obras de edificação (faixa média do TCU). */
    public static ComposicaoBdi padrao() {
        return new ComposicaoBdi(Percentual.de("4.00"), Percentual.de("0.80"), Percentual.de("1.27"),
                Percentual.de("1.23"), Percentual.de("7.40"), Percentual.de("8.65"));
    }

    /** Multiplicador dos custos indiretos sobre o custo direto: (1 + AC + S + R) × (1 + DF). */
    public BigDecimal fatorIndiretos() {
        BigDecimal admRisco = BigDecimal.ONE.add(administracaoCentral.fracao())
                .add(seguroEGarantia.fracao()).add(risco.fracao());
        return admRisco.multiply(BigDecimal.ONE.add(despesasFinanceiras.fracao()), MC);
    }

    public BigDecimal fatorPrecoDeVenda() {
        return fatorIndiretos()
                .multiply(BigDecimal.ONE.add(lucro.fracao()), MC)
                .divide(BigDecimal.ONE.subtract(impostos.fracao()), MC);
    }

    public Percentual bdi() {
        return new Percentual(fatorPrecoDeVenda().subtract(BigDecimal.ONE));
    }

    public Dinheiro precoDeVenda(Dinheiro custoDireto) {
        return custoDireto.multiplicar(fatorPrecoDeVenda());
    }
}
