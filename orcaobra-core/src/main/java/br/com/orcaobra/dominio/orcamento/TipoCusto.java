package br.com.orcaobra.dominio.orcamento;

/** Categoria de um custo, previsto ou realizado. */
public enum TipoCusto {
    MATERIAL("Material"),
    MAO_DE_OBRA("Mão de obra"),
    OUTROS("Outros");

    private final String descricao;

    TipoCusto(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
