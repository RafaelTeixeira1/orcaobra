package br.com.orcaobra.api.adaptador.entrada.web.dto;

import br.com.orcaobra.api.adaptador.entrada.web.dto.Requisicoes.BdiDto;
import br.com.orcaobra.dominio.catalogo.FuncaoMaoDeObra;
import br.com.orcaobra.dominio.catalogo.Material;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.orcamento.AnaliseDeMargem;
import br.com.orcaobra.dominio.orcamento.Cliente;
import br.com.orcaobra.dominio.orcamento.CustoRealizado;
import br.com.orcaobra.dominio.orcamento.ItemListaDeMateriais;
import br.com.orcaobra.dominio.orcamento.ItemOrcamento;
import br.com.orcaobra.dominio.orcamento.LinhaDeCusto;
import br.com.orcaobra.dominio.orcamento.Obra;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import br.com.orcaobra.dominio.orcamento.StatusOrcamento;
import br.com.orcaobra.dominio.orcamento.TipoCusto;
import br.com.orcaobra.dominio.servico.ParametroServico;
import br.com.orcaobra.dominio.servico.TipoServico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Corpos das respostas HTTP (valores em reais e percentuais em pontos). */
public final class Respostas {

    private Respostas() {
    }

    private static BigDecimal v(Dinheiro d) {
        return d == null ? null : d.valor();
    }

    public record OrcamentoResumo(UUID id, LocalDate dataCriacao, String cliente, String obra,
                                  StatusOrcamento status, String statusDescricao,
                                  BigDecimal custoDireto, BigDecimal precoDeVenda) {
        public static OrcamentoResumo de(Orcamento o) {
            return new OrcamentoResumo(o.id(), o.dataCriacao(), o.cliente().nome(), o.obra().descricao(),
                    o.status(), o.status().descricao(), v(o.custoDireto()), v(o.precoDeVenda()));
        }
    }

    public record Linha(TipoCusto tipo, String codigo, String descricao, String unidade, BigDecimal quantidade,
                        BigDecimal precoUnitario, BigDecimal total) {
        static Linha de(LinhaDeCusto l) {
            return new Linha(l.tipo(), l.codigo(), l.descricao(), l.unidade().simbolo(),
                    l.quantidade().setScale(2, RoundingMode.HALF_UP), v(l.precoUnitario()), v(l.total()));
        }
    }

    public record Item(UUID id, TipoServico tipo, String tipoDescricao, String descricao, BigDecimal quantidade,
                       String unidade, Map<String, BigDecimal> parametros, BigDecimal custoMateriais,
                       BigDecimal custoMaoDeObra, BigDecimal custoDireto, List<Linha> linhas) {
        static Item de(ItemOrcamento i) {
            return new Item(i.id(), i.tipo(), i.tipo().descricao(), i.descricao(), i.parametros().quantidade(),
                    i.tipo().unidade().simbolo(), i.parametros().extras(), v(i.custo(TipoCusto.MATERIAL)),
                    v(i.custo(TipoCusto.MAO_DE_OBRA)), v(i.custoDireto()), i.linhas().stream().map(Linha::de).toList());
        }
    }

    public record Custo(UUID id, LocalDate data, TipoCusto categoria, String descricao, BigDecimal valor) {
        static Custo de(CustoRealizado c) {
            return new Custo(c.id(), c.data(), c.categoria(), c.descricao(), v(c.valor()));
        }
    }

    public record Margem(BigDecimal custoDiretoPrevisto, BigDecimal custosIndiretos, BigDecimal impostos,
                         BigDecimal precoDeVenda, BigDecimal bdi, BigDecimal lucroPrevisto,
                         BigDecimal margemPrevista, Map<TipoCusto, BigDecimal> previstoPorCategoria,
                         Map<TipoCusto, BigDecimal> realizadoPorCategoria, BigDecimal custoDiretoRealizado,
                         BigDecimal custoDiretoProjetado, BigDecimal lucroProjetado, BigDecimal margemProjetada,
                         BigDecimal percentualGasto, BigDecimal margemMinima, boolean abaixoDaMargemMinima) {
        public static Margem de(AnaliseDeMargem a) {
            return new Margem(v(a.custoDiretoPrevisto()), v(a.custosIndiretos()), v(a.impostos()),
                    v(a.precoDeVenda()), a.bdi().emPontos(), v(a.lucroPrevisto()), a.margemPrevista().emPontos(),
                    porCategoria(a.previstoPorCategoria()), porCategoria(a.realizadoPorCategoria()),
                    v(a.custoDiretoRealizado()), v(a.custoDiretoProjetado()), v(a.lucroProjetado()),
                    a.margemProjetada().emPontos(), a.percentualGasto().emPontos(), a.margemMinima().emPontos(),
                    a.abaixoDaMargemMinima());
        }

        private static Map<TipoCusto, BigDecimal> porCategoria(Map<TipoCusto, Dinheiro> m) {
            var r = new java.util.EnumMap<TipoCusto, BigDecimal>(TipoCusto.class);
            m.forEach((k, val) -> r.put(k, val.valor()));
            return r;
        }
    }

    public record OrcamentoDetalhe(UUID id, LocalDate dataCriacao, Cliente cliente, Obra obra, BdiDto bdi,
                                   BigDecimal margemMinima, int validadeDias, StatusOrcamento status,
                                   String statusDescricao, List<StatusOrcamento> proximosStatus,
                                   boolean editavel, boolean permiteLancarCustos, List<Item> itens,
                                   List<Custo> custosRealizados, Margem margem) {
        public static OrcamentoDetalhe de(Orcamento o) {
            return new OrcamentoDetalhe(o.id(), o.dataCriacao(), o.cliente(), o.obra(), BdiDto.de(o.bdi()),
                    o.margemMinima().emPontos(), o.validadeDias(), o.status(), o.status().descricao(),
                    o.status().proximos().stream().sorted(Comparator.comparingInt(StatusOrcamento::ordinal)).toList(),
                    o.status().permiteEditarItens(), o.status().permiteLancarCustos(),
                    o.itens().stream().map(Item::de).toList(),
                    o.custosRealizados().stream().map(Custo::de).toList(),
                    Margem.de(o.analisarMargem()));
        }
    }

    public record MaterialCompra(String codigo, String descricao, String unidade, BigDecimal quantidade,
                                 BigDecimal quantidadeCompra, BigDecimal precoUnitario, BigDecimal total) {
        public static MaterialCompra de(ItemListaDeMateriais i) {
            return new MaterialCompra(i.codigo(), i.descricao(), i.unidade().simbolo(), i.quantidade(),
                    i.quantidadeCompra(), v(i.precoUnitario()), v(i.totalCompra()));
        }
    }

    public record TipoServicoInfo(TipoServico codigo, String descricao, String unidade,
                                  List<ParametroServico> parametros) {
        public static TipoServicoInfo de(TipoServico t) {
            return new TipoServicoInfo(t, t.descricao(), t.unidade().simbolo(), t.parametros());
        }
    }

    public record MaterialInfo(String codigo, String descricao, String unidade, BigDecimal preco) {
        public static MaterialInfo de(Material m) {
            return new MaterialInfo(m.codigo(), m.descricao(), m.unidade().simbolo(), v(m.precoUnitario()));
        }
    }

    public record FuncaoInfo(String codigo, String descricao, BigDecimal valorHora) {
        public static FuncaoInfo de(FuncaoMaoDeObra f) {
            return new FuncaoInfo(f.codigo(), f.descricao(), v(f.valorHora()));
        }
    }
}
