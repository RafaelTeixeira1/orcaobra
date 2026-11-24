package br.com.orcaobra.dominio.orcamento;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Percentual;

import java.util.Map;

/**
 * Fotografia financeira do orçamento: o que foi previsto, o que já foi gasto e qual a margem
 * de lucro projetada se a obra terminar com os desvios atuais.
 *
 * <p>Custo direto projetado, por categoria = máximo entre o previsto e o já realizado.
 * Margem = (preço − impostos − custos indiretos − custo direto) / preço.</p>
 */
public record AnaliseDeMargem(
        Dinheiro custoDiretoPrevisto,
        Dinheiro custosIndiretos,
        Dinheiro impostos,
        Dinheiro precoDeVenda,
        Percentual bdi,
        Dinheiro lucroPrevisto,
        Percentual margemPrevista,
        Map<TipoCusto, Dinheiro> previstoPorCategoria,
        Map<TipoCusto, Dinheiro> realizadoPorCategoria,
        Dinheiro custoDiretoRealizado,
        Dinheiro custoDiretoProjetado,
        Dinheiro lucroProjetado,
        Percentual margemProjetada,
        Percentual percentualGasto,
        Percentual margemMinima,
        boolean abaixoDaMargemMinima) {
}
