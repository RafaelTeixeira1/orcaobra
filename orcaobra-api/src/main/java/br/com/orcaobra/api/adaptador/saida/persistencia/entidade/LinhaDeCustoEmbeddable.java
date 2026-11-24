package br.com.orcaobra.api.adaptador.saida.persistencia.entidade;

import br.com.orcaobra.dominio.comum.Unidade;
import br.com.orcaobra.dominio.orcamento.TipoCusto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

@Embeddable
public class LinhaDeCustoEmbeddable {

    @Enumerated(EnumType.STRING)
    private TipoCusto tipo;
    private String codigo;
    private String descricao;
    @Enumerated(EnumType.STRING)
    private Unidade unidade;
    private BigDecimal quantidade;
    @Column(name = "preco_unitario")
    private BigDecimal precoUnitario;

    protected LinhaDeCustoEmbeddable() {
    }

    public LinhaDeCustoEmbeddable(TipoCusto tipo, String codigo, String descricao, Unidade unidade,
                                  BigDecimal quantidade, BigDecimal precoUnitario) {
        this.tipo = tipo;
        this.codigo = codigo;
        this.descricao = descricao;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public TipoCusto getTipo() { return tipo; }
    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public Unidade getUnidade() { return unidade; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
}
