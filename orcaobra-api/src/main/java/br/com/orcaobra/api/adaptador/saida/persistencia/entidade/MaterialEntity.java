package br.com.orcaobra.api.adaptador.saida.persistencia.entidade;

import br.com.orcaobra.dominio.comum.Unidade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "material")
public class MaterialEntity {

    @Id
    private String codigo;
    private String descricao;
    @Enumerated(EnumType.STRING)
    private Unidade unidade;
    @Column(name = "preco_unitario")
    private BigDecimal precoUnitario;

    protected MaterialEntity() {
    }

    public MaterialEntity(String codigo, String descricao, Unidade unidade, BigDecimal precoUnitario) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.unidade = unidade;
        this.precoUnitario = precoUnitario;
    }

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public Unidade getUnidade() { return unidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
}
