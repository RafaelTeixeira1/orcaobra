package br.com.orcaobra.api.adaptador.saida.persistencia.entidade;

import br.com.orcaobra.dominio.orcamento.StatusOrcamento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de persistência do orçamento. Fica separado do modelo de domínio para que o domínio
 * não dependa de JPA (anotações, construtor vazio, proxies...).
 */
@Entity
@Table(name = "orcamento")
public class OrcamentoEntity {

    @Id
    private UUID id;
    @Column(name = "data_criacao")
    private LocalDate dataCriacao;

    @Column(name = "cliente_nome") private String clienteNome;
    @Column(name = "cliente_documento") private String clienteDocumento;
    @Column(name = "cliente_email") private String clienteEmail;
    @Column(name = "cliente_telefone") private String clienteTelefone;

    @Column(name = "obra_descricao") private String obraDescricao;
    @Column(name = "obra_endereco") private String obraEndereco;
    @Column(name = "obra_cidade") private String obraCidade;
    @Column(name = "obra_area_m2") private BigDecimal obraAreaM2;

    @Column(name = "bdi_administracao") private BigDecimal bdiAdministracao;
    @Column(name = "bdi_seguro") private BigDecimal bdiSeguro;
    @Column(name = "bdi_risco") private BigDecimal bdiRisco;
    @Column(name = "bdi_despesas_financeiras") private BigDecimal bdiDespesasFinanceiras;
    @Column(name = "bdi_lucro") private BigDecimal bdiLucro;
    @Column(name = "bdi_impostos") private BigDecimal bdiImpostos;

    @Column(name = "margem_minima") private BigDecimal margemMinima;
    @Column(name = "validade_dias") private int validadeDias;

    @Enumerated(EnumType.STRING)
    private StatusOrcamento status;

    @Version
    private Long versao;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    private List<ItemOrcamentoEntity> itens = new ArrayList<>();

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("data, id")
    private List<CustoRealizadoEntity> custos = new ArrayList<>();

    protected OrcamentoEntity() {
    }

    public OrcamentoEntity(UUID id) {
        this.id = id;
    }

    public UUID getId() { return id; }
    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate v) { this.dataCriacao = v; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String v) { this.clienteNome = v; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String v) { this.clienteDocumento = v; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String v) { this.clienteEmail = v; }
    public String getClienteTelefone() { return clienteTelefone; }
    public void setClienteTelefone(String v) { this.clienteTelefone = v; }
    public String getObraDescricao() { return obraDescricao; }
    public void setObraDescricao(String v) { this.obraDescricao = v; }
    public String getObraEndereco() { return obraEndereco; }
    public void setObraEndereco(String v) { this.obraEndereco = v; }
    public String getObraCidade() { return obraCidade; }
    public void setObraCidade(String v) { this.obraCidade = v; }
    public BigDecimal getObraAreaM2() { return obraAreaM2; }
    public void setObraAreaM2(BigDecimal v) { this.obraAreaM2 = v; }
    public BigDecimal getBdiAdministracao() { return bdiAdministracao; }
    public void setBdiAdministracao(BigDecimal v) { this.bdiAdministracao = v; }
    public BigDecimal getBdiSeguro() { return bdiSeguro; }
    public void setBdiSeguro(BigDecimal v) { this.bdiSeguro = v; }
    public BigDecimal getBdiRisco() { return bdiRisco; }
    public void setBdiRisco(BigDecimal v) { this.bdiRisco = v; }
    public BigDecimal getBdiDespesasFinanceiras() { return bdiDespesasFinanceiras; }
    public void setBdiDespesasFinanceiras(BigDecimal v) { this.bdiDespesasFinanceiras = v; }
    public BigDecimal getBdiLucro() { return bdiLucro; }
    public void setBdiLucro(BigDecimal v) { this.bdiLucro = v; }
    public BigDecimal getBdiImpostos() { return bdiImpostos; }
    public void setBdiImpostos(BigDecimal v) { this.bdiImpostos = v; }
    public BigDecimal getMargemMinima() { return margemMinima; }
    public void setMargemMinima(BigDecimal v) { this.margemMinima = v; }
    public int getValidadeDias() { return validadeDias; }
    public void setValidadeDias(int v) { this.validadeDias = v; }
    public StatusOrcamento getStatus() { return status; }
    public void setStatus(StatusOrcamento v) { this.status = v; }
    public List<ItemOrcamentoEntity> getItens() { return itens; }
    public List<CustoRealizadoEntity> getCustos() { return custos; }
}
