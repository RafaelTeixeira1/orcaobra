package br.com.orcaobra.dominio.proposta;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Unidade;
import br.com.orcaobra.dominio.orcamento.Cliente;
import br.com.orcaobra.dominio.orcamento.Obra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Modelo da proposta comercial que o cliente recebe. Mostra o preço de cada serviço já com BDI
 * e esconde a composição de custos e a margem (informação interna do orçamentista).
 */
public record Proposta(String numero, LocalDate dataEmissao, LocalDate validaAte, DadosEmpresa empresa,
                       Cliente cliente, Obra obra, List<Item> itens, Dinheiro valorTotal,
                       Dinheiro valorPorM2, String condicoesPagamento, String prazoExecucao,
                       String observacoes) {

    public record Item(int ordem, String descricao, BigDecimal quantidade, Unidade unidade, Dinheiro valor) {
    }

    public Proposta {
        itens = List.copyOf(itens);
    }
}
