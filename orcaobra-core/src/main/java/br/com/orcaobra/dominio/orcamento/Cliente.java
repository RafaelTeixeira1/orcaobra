package br.com.orcaobra.dominio.orcamento;

import br.com.orcaobra.dominio.comum.RegraDeNegocioException;

/** Cliente para quem o orçamento é feito. */
public record Cliente(String nome, String documento, String email, String telefone) {

    public Cliente {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("O nome do cliente é obrigatório");
        }
        nome = nome.trim();
    }
}
