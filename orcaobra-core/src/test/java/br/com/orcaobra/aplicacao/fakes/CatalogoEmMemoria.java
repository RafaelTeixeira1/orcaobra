package br.com.orcaobra.aplicacao.fakes;

import br.com.orcaobra.Fixtures;
import br.com.orcaobra.aplicacao.porta.saida.CatalogoDePrecos;
import br.com.orcaobra.dominio.catalogo.FuncaoMaoDeObra;
import br.com.orcaobra.dominio.catalogo.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CatalogoEmMemoria implements CatalogoDePrecos {

    private final Map<String, Material> materiais = new LinkedHashMap<>();
    private final Map<String, FuncaoMaoDeObra> funcoes = new LinkedHashMap<>();

    public CatalogoEmMemoria() {
        Fixtures.materiais().forEach(this::salvar);
        Fixtures.funcoes().forEach(this::salvar);
    }

    @Override public Optional<Material> material(String codigo) { return Optional.ofNullable(materiais.get(codigo)); }
    @Override public Optional<FuncaoMaoDeObra> funcao(String codigo) { return Optional.ofNullable(funcoes.get(codigo)); }
    @Override public List<Material> materiais() { return new ArrayList<>(materiais.values()); }
    @Override public List<FuncaoMaoDeObra> funcoes() { return new ArrayList<>(funcoes.values()); }
    @Override public void salvar(Material m) { materiais.put(m.codigo(), m); }
    @Override public void salvar(FuncaoMaoDeObra f) { funcoes.put(f.codigo(), f); }
}
