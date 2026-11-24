package br.com.orcaobra.api.adaptador.entrada.web;

import br.com.orcaobra.api.adaptador.entrada.web.dto.Requisicoes.AdicionarServicoRequest;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Requisicoes.AlterarStatusRequest;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Requisicoes.CondicoesRequest;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Requisicoes.CriarOrcamentoRequest;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Requisicoes.LancarCustoRequest;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Respostas.MaterialCompra;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Respostas.Margem;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Respostas.OrcamentoDetalhe;
import br.com.orcaobra.api.adaptador.entrada.web.dto.Respostas.OrcamentoResumo;
import br.com.orcaobra.aplicacao.porta.entrada.AcompanharMargemUseCase;
import br.com.orcaobra.aplicacao.porta.entrada.GerarPropostaUseCase;
import br.com.orcaobra.aplicacao.porta.entrada.GerenciarOrcamentoUseCase;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Percentual;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/** Adaptador de entrada REST: traduz HTTP ↔ casos de uso. Não contém regra de negócio. */
@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final GerenciarOrcamentoUseCase gerenciar;
    private final AcompanharMargemUseCase margem;
    private final GerarPropostaUseCase proposta;

    public OrcamentoController(GerenciarOrcamentoUseCase gerenciar, AcompanharMargemUseCase margem,
                               GerarPropostaUseCase proposta) {
        this.gerenciar = gerenciar;
        this.margem = margem;
        this.proposta = proposta;
    }

    @GetMapping
    public List<OrcamentoResumo> listar() {
        return gerenciar.listar().stream().map(OrcamentoResumo::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrcamentoDetalhe criar(@Valid @RequestBody CriarOrcamentoRequest req) {
        var cmd = new GerenciarOrcamentoUseCase.CriarOrcamentoComando(
                req.cliente().paraDominio(), req.obra().paraDominio(),
                req.bdi() != null ? req.bdi().paraDominio() : null,
                req.margemMinima() != null ? Percentual.de(req.margemMinima()) : null,
                req.validadeDias());
        return OrcamentoDetalhe.de(gerenciar.criar(cmd));
    }

    @GetMapping("/{id}")
    public OrcamentoDetalhe buscar(@PathVariable UUID id) {
        return OrcamentoDetalhe.de(gerenciar.buscar(id));
    }

    @PostMapping("/{id}/servicos")
    @ResponseStatus(HttpStatus.CREATED)
    public OrcamentoDetalhe adicionarServico(@PathVariable UUID id, @Valid @RequestBody AdicionarServicoRequest req) {
        var cmd = new GerenciarOrcamentoUseCase.AdicionarServicoComando(req.tipo(), req.descricao(),
                req.quantidade(), req.parametros());
        return OrcamentoDetalhe.de(gerenciar.adicionarServico(id, cmd));
    }

    @DeleteMapping("/{id}/servicos/{itemId}")
    public OrcamentoDetalhe removerServico(@PathVariable UUID id, @PathVariable UUID itemId) {
        return OrcamentoDetalhe.de(gerenciar.removerServico(id, itemId));
    }

    @PutMapping("/{id}/condicoes")
    public OrcamentoDetalhe definirCondicoes(@PathVariable UUID id, @Valid @RequestBody CondicoesRequest req) {
        var cmd = new GerenciarOrcamentoUseCase.DefinirCondicoesComando(req.bdi().paraDominio(),
                Percentual.de(req.margemMinima()), req.validadeDias());
        return OrcamentoDetalhe.de(gerenciar.definirCondicoes(id, cmd));
    }

    @PatchMapping("/{id}/status")
    public OrcamentoDetalhe alterarStatus(@PathVariable UUID id, @Valid @RequestBody AlterarStatusRequest req) {
        return OrcamentoDetalhe.de(gerenciar.alterarStatus(id, req.status()));
    }

    @GetMapping("/{id}/materiais")
    public List<MaterialCompra> listaDeMateriais(@PathVariable UUID id) {
        return gerenciar.buscar(id).listaDeMateriais().stream().map(MaterialCompra::de).toList();
    }

    @GetMapping("/{id}/margem")
    public Margem analisarMargem(@PathVariable UUID id) {
        return Margem.de(margem.analisar(id));
    }

    @PostMapping("/{id}/custos")
    @ResponseStatus(HttpStatus.CREATED)
    public Margem lancarCusto(@PathVariable UUID id, @Valid @RequestBody LancarCustoRequest req) {
        var cmd = new AcompanharMargemUseCase.LancarCustoComando(req.data(), req.categoria(), req.descricao(),
                Dinheiro.de(req.valor()));
        return Margem.de(margem.lancarCusto(id, cmd));
    }

    @GetMapping(value = "/{id}/proposta.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> proposta(@PathVariable UUID id,
                                           @RequestParam(required = false) String condicoesPagamento,
                                           @RequestParam(required = false) String prazoExecucao,
                                           @RequestParam(required = false) String observacoes) {
        var doc = proposta.gerar(new GerarPropostaUseCase.GerarPropostaComando(id, condicoesPagamento,
                prazoExecucao, observacoes));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(doc.nomeArquivo(), StandardCharsets.UTF_8).build().toString())
                .body(doc.conteudo());
    }
}
