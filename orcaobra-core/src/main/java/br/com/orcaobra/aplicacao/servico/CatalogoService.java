package br.com.orcaobra.aplicacao.servico;

import br.com.orcaobra.aplicacao.porta.entrada.CatalogoUseCase;
import br.com.orcaobra.aplicacao.porta.saida.CatalogoDePrecos;
import br.com.orcaobra.dominio.catalogo.FuncaoMaoDeObra;
import br.com.orcaobra.dominio.catalogo.Material;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.RecursoNaoEncontradoException;

import java.util.List;

public class CatalogoService implements CatalogoUseCase {

    private final CatalogoDePrecos catalogo;

    public CatalogoService(CatalogoDePrecos catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public List<Material> materiais() {
        return catalogo.materiais();
    }

    @Override
    public List<FuncaoMaoDeObra> funcoes() {
        return catalogo.funcoes();
    }

    @Override
    public Material atualizarPrecoMaterial(String codigo, Dinheiro novoPreco) {
        Material atualizado = catalogo.material(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material não cadastrado: " + codigo))
                .comPreco(novoPreco);
        catalogo.salvar(atualizado);
        return atualizado;
    }

    @Override
    public FuncaoMaoDeObra atualizarValorHora(String codigo, Dinheiro novoValor) {
        FuncaoMaoDeObra atualizada = catalogo.funcao(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Função não cadastrada: " + codigo))
                .comValorHora(novoValor);
        catalogo.salvar(atualizada);
        return atualizada;
    }
}
