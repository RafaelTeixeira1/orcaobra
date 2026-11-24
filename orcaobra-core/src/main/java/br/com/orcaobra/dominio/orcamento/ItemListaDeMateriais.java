package br.com.orcaobra.dominio.orcamento;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Unidade;

import java.math.BigDecimal;

/** Linha da lista de compras consolidada do orçamento. */
public record ItemListaDeMateriais(String codigo, String descricao, Unidade unidade,
                                   BigDecimal quantidade, BigDecimal quantidadeCompra,
                                   Dinheiro precoUnitario, Dinheiro totalCompra) {
}
