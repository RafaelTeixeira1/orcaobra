package br.com.orcaobra.dominio.orcamento;

import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Percentual;
import br.com.orcaobra.dominio.comum.RecursoNaoEncontradoException;
import br.com.orcaobra.dominio.comum.RegraDeNegocioException;
import br.com.orcaobra.dominio.orcamento.eventos.EventoDeDominio;
import br.com.orcaobra.dominio.orcamento.eventos.MargemAbaixoDoMinimo;
import br.com.orcaobra.dominio.orcamento.eventos.StatusAlterado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Raiz de agregado (DDD). Toda alteração em itens, status, BDI ou custos realizados passa por aqui,
 * o que garante as regras de negócio num lugar só.
 */
public class Orcamento {

    private final UUID id;
    private final LocalDate dataCriacao;
    private Cliente cliente;
    private Obra obra;
    private ComposicaoBdi bdi;
    private Percentual margemMinima;
    private int validadeDias;
    private StatusOrcamento status;
    private final List<ItemOrcamento> itens;
    private final List<CustoRealizado> custosRealizados;
    private final List<EventoDeDominio> eventos = new ArrayList<>();

    private Orcamento(UUID id, LocalDate dataCriacao, Cliente cliente, Obra obra, ComposicaoBdi bdi,
                      Percentual margemMinima, int validadeDias, StatusOrcamento status,
                      List<ItemOrcamento> itens, List<CustoRealizado> custosRealizados) {
        this.id = Objects.requireNonNull(id);
        this.dataCriacao = Objects.requireNonNull(dataCriacao);
        this.cliente = Objects.requireNonNull(cliente, "o cliente é obrigatório");
        this.obra = Objects.requireNonNull(obra, "a obra é obrigatória");
        this.bdi = Objects.requireNonNull(bdi);
        this.margemMinima = Objects.requireNonNull(margemMinima);
        if (validadeDias <= 0) {
            throw new RegraDeNegocioException("A validade da proposta deve ser de pelo menos 1 dia");
        }
        this.validadeDias = validadeDias;
        this.status = Objects.requireNonNull(status);
        this.itens = new ArrayList<>(itens);
        this.custosRealizados = new ArrayList<>(custosRealizados);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Recria um orçamento já existente (usado pelos repositórios). */
    public static Orcamento reconstituir(UUID id, LocalDate dataCriacao, Cliente cliente, Obra obra,
                                         ComposicaoBdi bdi, Percentual margemMinima, int validadeDias,
                                         StatusOrcamento status, List<ItemOrcamento> itens,
                                         List<CustoRealizado> custos) {
        return new Orcamento(id, dataCriacao, cliente, obra, bdi, margemMinima, validadeDias, status, itens, custos);
    }

    // ---------------------------------------------------------------- comportamento

    public void adicionarItem(ItemOrcamento item) {
        exigirEdicao();
        itens.add(Objects.requireNonNull(item));
    }

    public void removerItem(UUID itemId) {
        exigirEdicao();
        if (!itens.removeIf(i -> i.id().equals(itemId))) {
            throw new RecursoNaoEncontradoException("Item " + itemId + " não encontrado no orçamento");
        }
    }

    public void definirCondicoes(ComposicaoBdi novoBdi, Percentual novaMargemMinima, int novaValidadeDias) {
        exigirEdicao();
        if (novaValidadeDias <= 0) {
            throw new RegraDeNegocioException("A validade da proposta deve ser de pelo menos 1 dia");
        }
        this.bdi = Objects.requireNonNull(novoBdi);
        this.margemMinima = Objects.requireNonNull(novaMargemMinima);
        this.validadeDias = novaValidadeDias;
    }

    public void atualizarDados(Cliente novoCliente, Obra novaObra) {
        exigirEdicao();
        this.cliente = Objects.requireNonNull(novoCliente);
        this.obra = Objects.requireNonNull(novaObra);
    }

    public void alterarStatus(StatusOrcamento novo, Instant agora) {
        if (!status.podeIrPara(novo)) {
            throw new RegraDeNegocioException(
                    "Não é possível mudar de '" + status.descricao() + "' para '" + novo.descricao() + "'");
        }
        if (novo == StatusOrcamento.ENVIADO && itens.isEmpty()) {
            throw new RegraDeNegocioException("Adicione ao menos um serviço antes de enviar o orçamento");
        }
        StatusOrcamento anterior = status;
        status = novo;
        eventos.add(new StatusAlterado(id, anterior, novo, agora));
    }

    public void lancarCusto(CustoRealizado custo, Instant agora) {
        if (!status.permiteLancarCustos()) {
            throw new RegraDeNegocioException("Custos reais só podem ser lançados com a obra em execução");
        }
        boolean estavaAbaixo = analisarMargem().abaixoDaMargemMinima();
        custosRealizados.add(Objects.requireNonNull(custo));
        AnaliseDeMargem depois = analisarMargem();
        if (!estavaAbaixo && depois.abaixoDaMargemMinima()) {
            eventos.add(new MargemAbaixoDoMinimo(id, depois.margemProjetada(), margemMinima, agora));
        }
    }

    // ---------------------------------------------------------------- cálculos

    public Dinheiro custoDireto() {
        return itens.stream().map(ItemOrcamento::custoDireto).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public Dinheiro custo(TipoCusto tipo) {
        return itens.stream().map(i -> i.custo(tipo)).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public Dinheiro precoDeVenda() {
        return bdi.precoDeVenda(custoDireto());
    }

    public LocalDate validoAte(LocalDate dataEmissao) {
        return dataEmissao.plusDays(validadeDias);
    }

    public AnaliseDeMargem analisarMargem() {
        Dinheiro custoDireto = custoDireto();
        Dinheiro preco = precoDeVenda();
        Dinheiro impostos = preco.multiplicar(bdi.impostos());
        Dinheiro indiretos = custoDireto.multiplicar(bdi.fatorIndiretos()).subtrair(custoDireto);
        Dinheiro receitaLiquida = preco.subtrair(impostos);
        Dinheiro lucroPrevisto = receitaLiquida.subtrair(indiretos).subtrair(custoDireto);

        Map<TipoCusto, Dinheiro> previsto = new EnumMap<>(TipoCusto.class);
        Map<TipoCusto, Dinheiro> realizado = new EnumMap<>(TipoCusto.class);
        Dinheiro projetado = Dinheiro.ZERO;
        for (TipoCusto tipo : TipoCusto.values()) {
            Dinheiro p = custo(tipo);
            Dinheiro r = custosRealizados.stream().filter(c -> c.categoria() == tipo)
                    .map(CustoRealizado::valor).reduce(Dinheiro.ZERO, Dinheiro::somar);
            previsto.put(tipo, p);
            realizado.put(tipo, r);
            projetado = projetado.somar(Dinheiro.max(p, r));
        }
        Dinheiro totalRealizado = realizado.values().stream().reduce(Dinheiro.ZERO, Dinheiro::somar);
        Dinheiro lucroProjetado = receitaLiquida.subtrair(indiretos).subtrair(projetado);
        Percentual margemPrevista = lucroPrevisto.proporcaoDe(preco);
        Percentual margemProjetada = lucroProjetado.proporcaoDe(preco);
        boolean abaixo = !itens.isEmpty() && margemProjetada.menorQue(margemMinima);

        return new AnaliseDeMargem(custoDireto, indiretos, impostos, preco, bdi.bdi(), lucroPrevisto,
                margemPrevista, Collections.unmodifiableMap(previsto), Collections.unmodifiableMap(realizado),
                totalRealizado, projetado, lucroProjetado, margemProjetada,
                totalRealizado.proporcaoDe(custoDireto), margemMinima, abaixo);
    }

    /** Lista de compras: soma o mesmo material de todos os serviços e arredonda para a compra. */
    public List<ItemListaDeMateriais> listaDeMateriais() {
        Map<String, List<LinhaDeCusto>> porCodigo = new LinkedHashMap<>();
        itens.stream().flatMap(i -> i.linhas().stream())
                .filter(l -> l.tipo() == TipoCusto.MATERIAL)
                .forEach(l -> porCodigo.computeIfAbsent(l.codigo(), k -> new ArrayList<>()).add(l));

        List<ItemListaDeMateriais> lista = new ArrayList<>();
        porCodigo.forEach((codigo, linhas) -> {
            LinhaDeCusto ref = linhas.get(linhas.size() - 1);
            BigDecimal qtd = linhas.stream().map(LinhaDeCusto::quantidade).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal compra = ref.unidade().discreta()
                    ? qtd.setScale(0, RoundingMode.CEILING)
                    : qtd.setScale(2, RoundingMode.CEILING);
            lista.add(new ItemListaDeMateriais(codigo, ref.descricao(), ref.unidade(), qtd.setScale(2, RoundingMode.HALF_UP),
                    compra, ref.precoUnitario(), ref.precoUnitario().multiplicar(compra)));
        });
        return lista;
    }

    /** Entrega os eventos registrados e limpa a fila (chamado pela camada de aplicação). */
    public List<EventoDeDominio> retirarEventos() {
        List<EventoDeDominio> copia = List.copyOf(eventos);
        eventos.clear();
        return copia;
    }

    private void exigirEdicao() {
        if (!status.permiteEditarItens()) {
            throw new RegraDeNegocioException(
                    "O orçamento está '" + status.descricao() + "'. Volte para rascunho para editar.");
        }
    }

    // ---------------------------------------------------------------- getters

    public UUID id() { return id; }
    public LocalDate dataCriacao() { return dataCriacao; }
    public Cliente cliente() { return cliente; }
    public Obra obra() { return obra; }
    public ComposicaoBdi bdi() { return bdi; }
    public Percentual margemMinima() { return margemMinima; }
    public int validadeDias() { return validadeDias; }
    public StatusOrcamento status() { return status; }
    public List<ItemOrcamento> itens() { return List.copyOf(itens); }
    public List<CustoRealizado> custosRealizados() { return List.copyOf(custosRealizados); }

    // ---------------------------------------------------------------- builder

    /** Padrão Builder: cria orçamentos novos com valores padrão sensatos. */
    public static final class Builder {
        private UUID id = UUID.randomUUID();
        private LocalDate dataCriacao = LocalDate.now();
        private Cliente cliente;
        private Obra obra;
        private ComposicaoBdi bdi = ComposicaoBdi.padrao();
        private Percentual margemMinima = Percentual.de("5");
        private int validadeDias = 15;

        private Builder() {
        }

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder dataCriacao(LocalDate data) { this.dataCriacao = data; return this; }
        public Builder cliente(Cliente cliente) { this.cliente = cliente; return this; }
        public Builder obra(Obra obra) { this.obra = obra; return this; }
        public Builder bdi(ComposicaoBdi bdi) { this.bdi = bdi; return this; }
        public Builder margemMinima(Percentual margem) { this.margemMinima = margem; return this; }
        public Builder validadeDias(int dias) { this.validadeDias = dias; return this; }

        public Orcamento build() {
            return new Orcamento(id, dataCriacao, cliente, obra, bdi, margemMinima, validadeDias,
                    StatusOrcamento.RASCUNHO, List.of(), List.of());
        }
    }
}
