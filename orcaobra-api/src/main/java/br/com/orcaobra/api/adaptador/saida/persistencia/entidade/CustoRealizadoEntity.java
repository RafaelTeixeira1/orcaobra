package br.com.orcaobra.api.adaptador.saida.persistencia.entidade;

import br.com.orcaobra.dominio.orcamento.TipoCusto;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "custo_realizado")
public class CustoRealizadoEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id")
    private OrcamentoEntity orcamento;

    private LocalDate data;

    @Enumerated(EnumType.STRING)
    private TipoCusto categoria;

    private String descricao;

    private BigDecimal valor;

    protected CustoRealizadoEntity() {
    }

    public CustoRealizadoEntity(UUID id, OrcamentoEntity orcamento, LocalDate data, TipoCusto categoria,
                                String descricao, BigDecimal valor) {
        this.id = id;
        this.orcamento = orcamento;
        this.data = data;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
    }

    public UUID getId() { return id; }
    public LocalDate getData() { return data; }
    public TipoCusto getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
}
