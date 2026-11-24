-- Catálogo de preços ------------------------------------------------------------
CREATE TABLE material (
    codigo          VARCHAR(60)    PRIMARY KEY,
    descricao       VARCHAR(200)   NOT NULL,
    unidade         VARCHAR(10)    NOT NULL,
    preco_unitario  NUMERIC(19, 2) NOT NULL
);

CREATE TABLE funcao_mao_de_obra (
    codigo      VARCHAR(60)    PRIMARY KEY,
    descricao   VARCHAR(200)   NOT NULL,
    valor_hora  NUMERIC(19, 2) NOT NULL
);

-- Orçamento (raiz do agregado) ------------------------------------------------------
CREATE TABLE orcamento (
    id                        UUID           PRIMARY KEY,
    data_criacao              DATE           NOT NULL,
    cliente_nome              VARCHAR(200)   NOT NULL,
    cliente_documento         VARCHAR(30),
    cliente_email             VARCHAR(200),
    cliente_telefone          VARCHAR(30),
    obra_descricao            VARCHAR(300)   NOT NULL,
    obra_endereco             VARCHAR(300),
    obra_cidade               VARCHAR(120),
    obra_area_m2              NUMERIC(12, 2),
    bdi_administracao         NUMERIC(9, 6)  NOT NULL,
    bdi_seguro                NUMERIC(9, 6)  NOT NULL,
    bdi_risco                 NUMERIC(9, 6)  NOT NULL,
    bdi_despesas_financeiras  NUMERIC(9, 6)  NOT NULL,
    bdi_lucro                 NUMERIC(9, 6)  NOT NULL,
    bdi_impostos              NUMERIC(9, 6)  NOT NULL,
    margem_minima             NUMERIC(9, 6)  NOT NULL,
    validade_dias             INTEGER        NOT NULL,
    status                    VARCHAR(20)    NOT NULL,
    versao                    BIGINT         NOT NULL
);

CREATE TABLE item_orcamento (
    id            UUID           PRIMARY KEY,
    orcamento_id  UUID           NOT NULL REFERENCES orcamento (id) ON DELETE CASCADE,
    ordem         INTEGER        NOT NULL,
    tipo          VARCHAR(40)    NOT NULL,
    descricao     VARCHAR(300)   NOT NULL,
    quantidade    NUMERIC(19, 4) NOT NULL
);
CREATE INDEX idx_item_orcamento ON item_orcamento (orcamento_id);

CREATE TABLE item_parametro (
    item_id  UUID           NOT NULL REFERENCES item_orcamento (id) ON DELETE CASCADE,
    nome     VARCHAR(60)    NOT NULL,
    valor    NUMERIC(19, 4) NOT NULL,
    PRIMARY KEY (item_id, nome)
);

CREATE TABLE item_linha_custo (
    item_id         UUID           NOT NULL REFERENCES item_orcamento (id) ON DELETE CASCADE,
    posicao         INTEGER        NOT NULL,
    tipo            VARCHAR(20)    NOT NULL,
    codigo          VARCHAR(60)    NOT NULL,
    descricao       VARCHAR(200)   NOT NULL,
    unidade         VARCHAR(10)    NOT NULL,
    quantidade      NUMERIC(19, 4) NOT NULL,
    preco_unitario  NUMERIC(19, 2) NOT NULL,
    PRIMARY KEY (item_id, posicao)
);

CREATE TABLE custo_realizado (
    id            UUID           PRIMARY KEY,
    orcamento_id  UUID           NOT NULL REFERENCES orcamento (id) ON DELETE CASCADE,
    data          DATE           NOT NULL,
    categoria     VARCHAR(20)    NOT NULL,
    descricao     VARCHAR(300)   NOT NULL,
    valor         NUMERIC(19, 2) NOT NULL
);
CREATE INDEX idx_custo_orcamento ON custo_realizado (orcamento_id);
