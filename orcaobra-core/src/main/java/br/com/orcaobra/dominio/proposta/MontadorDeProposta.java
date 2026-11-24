package br.com.orcaobra.dominio.proposta;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.RegraDeNegocioException;
import br.com.orcaobra.dominio.orcamento.ItemOrcamento;
import br.com.orcaobra.dominio.orcamento.Orcamento;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de domínio que transforma um {@link Orcamento} em {@link Proposta}.
 * Distribui o BDI proporcionalmente entre os itens e ajusta o último item para que a soma
 * bata exatamente com o preço de venda (evita diferença de centavos no PDF).
 */
public final class MontadorDeProposta {

    public Proposta montar(Orcamento orcamento, DadosEmpresa empresa, LocalDate dataEmissao,
                           String condicoesPagamento, String prazoExecucao, String observacoes) {
        if (!orcamento.status().permiteGerarProposta()) {
            throw new RegraDeNegocioException("Não é possível gerar proposta de um orçamento "
                    + orcamento.status().descricao().toLowerCase());
        }
        List<ItemOrcamento> itens = orcamento.itens();
        if (itens.isEmpty()) {
            throw new RegraDeNegocioException("O orçamento não tem serviços para gerar a proposta");
        }

        Dinheiro total = orcamento.precoDeVenda();
        var fator = orcamento.bdi().fatorPrecoDeVenda();
        List<Proposta.Item> linhas = new ArrayList<>();
        Dinheiro acumulado = Dinheiro.ZERO;
        for (int i = 0; i < itens.size(); i++) {
            ItemOrcamento item = itens.get(i);
            Dinheiro valor = (i == itens.size() - 1)
                    ? total.subtrair(acumulado)
                    : item.custoDireto().multiplicar(fator);
            acumulado = acumulado.somar(valor);
            linhas.add(new Proposta.Item(i + 1, item.descricao(), item.parametros().quantidade(),
                    item.tipo().unidade(), valor));
        }

        Dinheiro porM2 = null;
        if (orcamento.obra().areaConstruidaM2() != null) {
            porM2 = Dinheiro.de(total.valor().divide(orcamento.obra().areaConstruidaM2(),
                    MathContext.DECIMAL64).setScale(2, RoundingMode.HALF_EVEN));
        }

        return new Proposta(numero(orcamento, dataEmissao), dataEmissao, orcamento.validoAte(dataEmissao),
                empresa, orcamento.cliente(), orcamento.obra(), linhas, total, porM2,
                condicoesPagamento, prazoExecucao, observacoes);
    }

    private static String numero(Orcamento o, LocalDate data) {
        return "ORC-" + data.getYear() + "-" + o.id().toString().substring(0, 8).toUpperCase();
    }
}
