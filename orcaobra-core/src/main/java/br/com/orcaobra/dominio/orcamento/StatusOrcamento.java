package br.com.orcaobra.dominio.orcamento;

import java.util.Set;

/**
 * Padrão State (implementado com enum): cada estado decide o que é permitido e para onde
 * o orçamento pode ir. O {@link Orcamento} delega essas decisões ao estado atual.
 *
 * <pre>
 * RASCUNHO → ENVIADO → APROVADO → EM_EXECUCAO → CONCLUIDO
 *     ↑          ↓
 *     └──── RECUSADO          (RASCUNHO, ENVIADO e APROVADO podem ser CANCELADOS)
 * </pre>
 */
public enum StatusOrcamento {

    RASCUNHO("Rascunho") {
        @Override public boolean permiteEditarItens() { return true; }
        @Override public Set<StatusOrcamento> proximos() { return Set.of(ENVIADO, CANCELADO); }
    },
    ENVIADO("Enviado ao cliente") {
        @Override public Set<StatusOrcamento> proximos() { return Set.of(APROVADO, RECUSADO, RASCUNHO, CANCELADO); }
    },
    APROVADO("Aprovado") {
        @Override public Set<StatusOrcamento> proximos() { return Set.of(EM_EXECUCAO, CANCELADO); }
    },
    RECUSADO("Recusado") {
        @Override public Set<StatusOrcamento> proximos() { return Set.of(RASCUNHO); }
        @Override public boolean permiteGerarProposta() { return false; }
    },
    EM_EXECUCAO("Em execução") {
        @Override public boolean permiteLancarCustos() { return true; }
        @Override public Set<StatusOrcamento> proximos() { return Set.of(CONCLUIDO); }
    },
    CONCLUIDO("Concluído") {
        @Override public Set<StatusOrcamento> proximos() { return Set.of(); }
    },
    CANCELADO("Cancelado") {
        @Override public Set<StatusOrcamento> proximos() { return Set.of(); }
        @Override public boolean permiteGerarProposta() { return false; }
    };

    private final String descricao;

    StatusOrcamento(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }

    public abstract Set<StatusOrcamento> proximos();

    public boolean permiteEditarItens() {
        return false;
    }

    public boolean permiteLancarCustos() {
        return false;
    }

    public boolean permiteGerarProposta() {
        return true;
    }

    public boolean podeIrPara(StatusOrcamento novo) {
        return proximos().contains(novo);
    }
}
