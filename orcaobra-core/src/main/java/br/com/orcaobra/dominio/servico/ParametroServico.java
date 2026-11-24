package br.com.orcaobra.dominio.servico;

import java.math.BigDecimal;

/** Descreve um parâmetro opcional de um serviço (ex.: espessura) e o seu valor padrão. */
public record ParametroServico(String nome, String descricao, BigDecimal padrao) {
}
