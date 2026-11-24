package br.com.orcaobra.dominio.catalogo;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.RegraDeNegocioException;
import br.com.orcaobra.dominio.comum.Unidade;

import java.util.Objects;

/** Material do catálogo de preços (cimento, areia, bloco...). */
public record Material(String codigo, String descricao, Unidade unidade, Dinheiro precoUnitario) {

    public Material {
        Objects.requireNonNull(codigo, "código é obrigatório");
        Objects.requireNonNull(descricao, "descrição é obrigatória");
        Objects.requireNonNull(unidade, "unidade é obrigatória");
        Objects.requireNonNull(precoUnitario, "preço é obrigatório");
        if (precoUnitario.ehNegativo()) {
            throw new RegraDeNegocioException("Preço do material não pode ser negativo");
        }
    }

    public Material comPreco(Dinheiro novoPreco) {
        return new Material(codigo, descricao, unidade, novoPreco);
    }
}
