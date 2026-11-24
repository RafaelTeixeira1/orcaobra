package br.com.orcaobra.aplicacao.porta.saida;

import br.com.orcaobra.dominio.catalogo.FuncaoMaoDeObra;
import br.com.orcaobra.dominio.catalogo.Material;

import java.util.List;
import java.util.Optional;

/** Porta de saída: tabela de preços de materiais e de mão de obra. */
public interface CatalogoDePrecos {

    Optional<Material> material(String codigo);

    Optional<FuncaoMaoDeObra> funcao(String codigo);

    List<Material> materiais();

    List<FuncaoMaoDeObra> funcoes();

    void salvar(Material material);

    void salvar(FuncaoMaoDeObra funcao);
}
