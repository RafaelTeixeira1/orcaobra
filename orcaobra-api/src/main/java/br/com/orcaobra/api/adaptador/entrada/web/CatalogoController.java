package br.com.orcaobra.api.adaptador.entrada.web;

import br.com.orcaobra.api.adaptador.entrada.web.dto.Requisicoes.AtualizarPrecoRequest;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Respostas.FuncaoInfo;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Respostas.MaterialInfo;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Respostas.TipoServicoInfo;
import br.com.orcaobra.aplicacao.porta.entrada.CatalogoUseCase;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.servico.TipoServico;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoUseCase catalogo;

    public CatalogoController(CatalogoUseCase catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/servicos")
    public List<TipoServicoInfo> tiposDeServico() {
        return Arrays.stream(TipoServico.values()).map(TipoServicoInfo::de).toList();
    }

    @GetMapping("/catalogo/materiais")
    public List<MaterialInfo> materiais() {
        return catalogo.materiais().stream().map(MaterialInfo::de).toList();
    }

    @PutMapping("/catalogo/materiais/{codigo}")
    public MaterialInfo atualizarMaterial(@PathVariable String codigo, @Valid @RequestBody AtualizarPrecoRequest req) {
        return MaterialInfo.de(catalogo.atualizarPrecoMaterial(codigo, Dinheiro.de(req.valor())));
    }

    @GetMapping("/catalogo/mao-de-obra")
    public List<FuncaoInfo> funcoes() {
        return catalogo.funcoes().stream().map(FuncaoInfo::de).toList();
    }

    @PutMapping("/catalogo/mao-de-obra/{codigo}")
    public FuncaoInfo atualizarFuncao(@PathVariable String codigo, @Valid @RequestBody AtualizarPrecoRequest req) {
        return FuncaoInfo.de(catalogo.atualizarValorHora(codigo, Dinheiro.de(req.valor())));
    }
}
