package br.com.orcaobra.api.adaptador.saida.persistencia.entidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "funcao_mao_de_obra")
public class FuncaoMaoDeObraEntity {

    @Id
    private String codigo;
    private String descricao;
    @Column(name = "valor_hora")
    private BigDecimal valorHora;

    protected FuncaoMaoDeObraEntity() {
    }

    public FuncaoMaoDeObraEntity(String codigo, String descricao, BigDecimal valorHora) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.valorHora = valorHora;
    }

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorHora() { return valorHora; }
}
