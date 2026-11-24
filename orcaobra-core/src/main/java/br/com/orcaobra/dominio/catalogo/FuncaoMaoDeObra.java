package br.com.orcaobra.dominio.catalogo;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.RegraDeNegocioException;

import java.util.Objects;

/** Função de mão de obra (pedreiro, servente, pintor) com o custo por hora, já com encargos. */
public record FuncaoMaoDeObra(String codigo, String descricao, Dinheiro valorHora) {

    public FuncaoMaoDeObra {
        Objects.requireNonNull(codigo, "código é obrigatório");
        Objects.requireNonNull(descricao, "descrição é obrigatória");
        Objects.requireNonNull(valorHora, "valor da hora é obrigatório");
        if (valorHora.ehNegativo()) {
            throw new RegraDeNegocioException("Valor da hora não pode ser negativo");
        }
    }

    public FuncaoMaoDeObra comValorHora(Dinheiro novoValor) {
        return new FuncaoMaoDeObra(codigo, descricao, novoValor);
    }
}
