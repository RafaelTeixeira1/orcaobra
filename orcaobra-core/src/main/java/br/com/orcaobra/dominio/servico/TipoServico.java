package br.com.orcaobra.dominio.servico;

import br.com.orcaobra.dominio.comum.Unidade;

import java.math.BigDecimal;
import java.util.List;

/** Serviços de obra que o sistema sabe calcular. Cada um tem uma {@link CalculadoraDeServico}. */
public enum TipoServico {

    CONCRETO_ESTRUTURAL("Concreto estrutural traço 1:2:3 (fundação, vigas, pilares, lajes)", Unidade.M3, List.of()),
    ALVENARIA("Alvenaria de vedação com bloco cerâmico 9x19x19 cm", Unidade.M2, List.of()),
    REBOCO("Chapisco e reboco de paredes", Unidade.M2,
            List.of(new ParametroServico("espessuraCm", "Espessura do reboco (cm)", new BigDecimal("2")))),
    CONTRAPISO("Contrapiso em argamassa 1:4", Unidade.M2,
            List.of(new ParametroServico("espessuraCm", "Espessura do contrapiso (cm)", new BigDecimal("5")))),
    PISO_CERAMICO("Revestimento cerâmico de piso", Unidade.M2,
            List.of(new ParametroServico("perdaPercentual", "Perda de cortes (%)", new BigDecimal("10")))),
    PINTURA("Pintura acrílica com selador", Unidade.M2,
            List.of(new ParametroServico("demaos", "Número de demãos de tinta", new BigDecimal("2"))));

    private final String descricao;
    private final Unidade unidade;
    private final List<ParametroServico> parametros;

    TipoServico(String descricao, Unidade unidade, List<ParametroServico> parametros) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.parametros = parametros;
    }

    public String descricao() {
        return descricao;
    }

    public Unidade unidade() {
        return unidade;
    }

    public List<ParametroServico> parametros() {
        return parametros;
    }
}
