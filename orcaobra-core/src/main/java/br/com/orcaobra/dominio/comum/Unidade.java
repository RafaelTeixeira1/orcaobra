package br.com.orcaobra.dominio.comum;

/** Unidades de medida usadas em insumos e serviços. */
public enum Unidade {
    UN("un", true),
    SC("saco", true),
    M("m", false),
    M2("m²", false),
    M3("m³", false),
    KG("kg", false),
    L("L", false),
    H("h", false);

    private final String simbolo;
    private final boolean discreta;

    Unidade(String simbolo, boolean discreta) {
        this.simbolo = simbolo;
        this.discreta = discreta;
    }

    public String simbolo() {
        return simbolo;
    }

    /** Unidades discretas só podem ser compradas em quantidades inteiras (blocos, sacos). */
    public boolean discreta() {
        return discreta;
    }
}
