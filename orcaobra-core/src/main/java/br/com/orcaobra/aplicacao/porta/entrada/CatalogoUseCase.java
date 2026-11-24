package br.com.orcaobra.aplicacao.porta.entrada;

import br.com.orcaobra.dominio.catalogo.FuncaoMaoDeObra;
import br.com.orcaobra.dominio.catalogo.Material;
import br.com.orcaobra.dominio.comum.Dinheiro;

import java.util.List;

/** Porta de entrada: consulta e atualização da tabela de preços. */
public interface CatalogoUseCase {

    List<Material> materiais();

    List<FuncaoMaoDeObra> funcoes();

    Material atualizarPrecoMaterial(String codigo, Dinheiro novoPreco);

    FuncaoMaoDeObra atualizarValorHora(String codigo, Dinheiro novoValor);
}
