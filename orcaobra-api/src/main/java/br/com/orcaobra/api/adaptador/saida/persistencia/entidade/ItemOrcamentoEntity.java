package br.com.orcaobra.api.adaptador.saida.persistencia.entidade;

import br.com.orcaobra.dominio.servico.TipoServico;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "item_orcamento")
public class ItemOrcamentoEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id")
    private OrcamentoEntity orcamento;

    private int ordem;

    @Enumerated(EnumType.STRING)
    private TipoServico tipo;

    private String descricao;

    private BigDecimal quantidade;

    @ElementCollection
    @CollectionTable(name = "item_parametro", joinColumns = @JoinColumn(name = "item_id"))
    @MapKeyColumn(name = "nome")
    @Column(name = "valor")
    private Map<String, BigDecimal> parametros = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "item_linha_custo", joinColumns = @JoinColumn(name = "item_id"))
    @OrderColumn(name = "posicao")
    private List<LinhaDeCustoEmbeddable> linhas = new ArrayList<>();

    protected ItemOrcamentoEntity() {
    }

    public ItemOrcamentoEntity(UUID id, OrcamentoEntity orcamento, int ordem, TipoServico tipo, String descricao,
                               BigDecimal quantidade, Map<String, BigDecimal> parametros,
                               List<LinhaDeCustoEmbeddable> linhas) {
        this.id = id;
        this.orcamento = orcamento;
        this.ordem = ordem;
        this.tipo = tipo;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.parametros = new HashMap<>(parametros);
        this.linhas = new ArrayList<>(linhas);
    }

    public UUID getId() { return id; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
    public TipoServico getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public BigDecimal getQuantidade() { return quantidade; }
    public Map<String, BigDecimal> getParametros() { return parametros; }
    public List<LinhaDeCustoEmbeddable> getLinhas() { return linhas; }
}
