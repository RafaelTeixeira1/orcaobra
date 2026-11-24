package br.com.orcaobra.api.adaptador.saida.persistencia;

import br.com.orcaobra.aplicacao.porta.saida.CatalogoDePrecos;
import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.FuncaoMaoDeObraEntity;
import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.MaterialEntity;
import br.com.orcaobra.dominio.catalogo.FuncaoMaoDeObra;
import br.com.orcaobra.dominio.catalogo.Material;
import br.com.orcaobra.dominio.comum.Dinheiro;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
class CatalogoJpaAdapter implements CatalogoDePrecos {

    private final MaterialJpaRepository materiais;
    private final FuncaoMaoDeObraJpaRepository funcoes;

    CatalogoJpaAdapter(MaterialJpaRepository materiais, FuncaoMaoDeObraJpaRepository funcoes) {
        this.materiais = materiais;
        this.funcoes = funcoes;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Material> material(String codigo) {
        return materiais.findById(codigo).map(CatalogoJpaAdapter::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FuncaoMaoDeObra> funcao(String codigo) {
        return funcoes.findById(codigo).map(CatalogoJpaAdapter::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> materiais() {
        return materiais.findAll(Sort.by("descricao")).stream().map(CatalogoJpaAdapter::paraDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuncaoMaoDeObra> funcoes() {
        return funcoes.findAll(Sort.by("descricao")).stream().map(CatalogoJpaAdapter::paraDominio).toList();
    }

    @Override
    @Transactional
    public void salvar(Material m) {
        materiais.save(new MaterialEntity(m.codigo(), m.descricao(), m.unidade(), m.precoUnitario().valor()));
    }

    @Override
    @Transactional
    public void salvar(FuncaoMaoDeObra f) {
        funcoes.save(new FuncaoMaoDeObraEntity(f.codigo(), f.descricao(), f.valorHora().valor()));
    }

    private static Material paraDominio(MaterialEntity e) {
        return new Material(e.getCodigo(), e.getDescricao(), e.getUnidade(), Dinheiro.de(e.getPrecoUnitario()));
    }

    private static FuncaoMaoDeObra paraDominio(FuncaoMaoDeObraEntity e) {
        return new FuncaoMaoDeObra(e.getCodigo(), e.getDescricao(), Dinheiro.de(e.getValorHora()));
    }
}
