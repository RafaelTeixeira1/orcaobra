package br.com.orcaobra.aplicacao.servico;

import br.com.orcaobra.aplicacao.porta.saida.CatalogoDePrecos;
import br.com.orcaobra.dominio.catalogo.FuncaoMaoDeObra;
import br.com.orcaobra.dominio.catalogo.Material;
import br.com.orcaobra.dominio.comum.RecursoNaoEncontradoException;
import br.com.orcaobra.dominio.comum.Unidade;
import br.com.orcaobra.dominio.orcamento.LinhaDeCusto;
import br.com.orcaobra.dominio.orcamento.TipoCusto;
import br.com.orcaobra.dominio.servico.ComposicaoServico;
import br.com.orcaobra.dominio.servico.ParametrosServico;
import br.com.orcaobra.dominio.servico.RegistroDeCalculadoras;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.util.ArrayList;
import java.util.List;

/**
 * Junta a estratégia de cálculo (quantidades) com o catálogo (preços) e produz as linhas de custo.
 */
public class PrecificadorDeServico {

    private final RegistroDeCalculadoras calculadoras;
    private final CatalogoDePrecos catalogo;

    public PrecificadorDeServico(RegistroDeCalculadoras calculadoras, CatalogoDePrecos catalogo) {
        this.calculadoras = calculadoras;
        this.catalogo = catalogo;
    }

    public List<LinhaDeCusto> precificar(TipoServico tipo, ParametrosServico parametros) {
        ComposicaoServico composicao = calculadoras.para(tipo).calcular(parametros);
        List<LinhaDeCusto> linhas = new ArrayList<>();
        for (ComposicaoServico.Consumo c : composicao.materiais()) {
            Material m = catalogo.material(c.codigo())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Material não cadastrado: " + c.codigo()));
            linhas.add(new LinhaDeCusto(TipoCusto.MATERIAL, m.codigo(), m.descricao(), m.unidade(),
                    c.quantidade(), m.precoUnitario()));
        }
        for (ComposicaoServico.Consumo c : composicao.maoDeObra()) {
            FuncaoMaoDeObra f = catalogo.funcao(c.codigo())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Função não cadastrada: " + c.codigo()));
            linhas.add(new LinhaDeCusto(TipoCusto.MAO_DE_OBRA, f.codigo(), f.descricao(), Unidade.H,
                    c.quantidade(), f.valorHora()));
        }
        return linhas;
    }
}
