# OrçaObra

Software para engenheiros e profissionais da construção **orçarem casas**: calcula materiais e mão de
obra por serviço, aplica BDI, gera **proposta profissional em PDF** e **acompanha a margem de lucro**
durante a obra, com alerta quando ela cai abaixo do mínimo.

**Stack:** Java 21 · Spring Boot 3.3 · Spring Data JPA · Flyway · H2/PostgreSQL · OpenPDF · JUnit 5 ·
ArchUnit · HTML/CSS/JS · Docker · GitHub Actions

## Funcionalidades

- Orçamento por serviços: concreto estrutural, alvenaria, reboco, contrapiso, piso cerâmico e pintura
- Cálculo automático de materiais (com perdas) e horas de pedreiro, servente e pintor
- Catálogo de preços editável; orçamentos guardam o preço da época
- BDI detalhado pela fórmula do TCU (administração, seguro, risco, despesas financeiras, lucro, impostos)
- Lista de compras consolidada (sacos e blocos arredondados para cima)
- Proposta comercial em PDF, sem expor custos internos
- Ciclo de vida: rascunho → enviado → aprovado → em execução → concluído
- Lançamento de custos reais, previsto × realizado e margem projetada com alerta

## Como rodar

Pré-requisitos: **JDK 21** e **Maven 3.9+** (ou Docker).

```bash
mvn verify                          # compila e roda todos os testes
mvn -pl orcaobra-api -am spring-boot:run
```

Acesse:

- Aplicação: <http://localhost:8080>
- Documentação da API (Swagger): <http://localhost:8080/swagger-ui.html>
- Console do banco H2: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:file:./data/orcaobra`, usuário `sa`)

Com PostgreSQL via Docker:

```bash
docker compose up --build
```

Os dados da empresa que aparecem no PDF ficam em `orcaobra-api/src/main/resources/application.yml`
(`orcaobra.empresa`).

## Estrutura

```
orcaobra/
├── orcaobra-core/            Java puro: domínio + casos de uso + portas
│   └── br/com/orcaobra/
│       ├── dominio/          Orcamento, ComposicaoBdi, StatusOrcamento, calculadoras, proposta
│       └── aplicacao/        portas de entrada/saída e serviços de aplicação
├── orcaobra-api/             Spring Boot: adaptadores
│   └── br/com/orcaobra/api/
│       ├── adaptador/entrada/web     REST + DTOs + tratamento de erros
│       ├── adaptador/saida/persistencia  JPA + mapeamento
│       ├── adaptador/saida/pdf       proposta em PDF (Template Method)
│       ├── adaptador/saida/eventos   alertas de margem (Observer)
│       └── config                    composition root
│   └── resources/
│       ├── db/migration      scripts Flyway
│       └── static            front-end
├── docs/                     requisitos e arquitetura (diagramas Mermaid)
└── estagio/                  registro diário e gerador do relatório de estágio
```

A arquitetura e os padrões de projeto estão explicados em [docs/arquitetura.md](docs/arquitetura.md);
os requisitos em [docs/requisitos.md](docs/requisitos.md).

## API (resumo)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/orcamentos` | Lista orçamentos |
| POST | `/api/orcamentos` | Cria orçamento |
| GET | `/api/orcamentos/{id}` | Detalhe com itens, custos e margem |
| POST | `/api/orcamentos/{id}/servicos` | Adiciona serviço (calcula e precifica) |
| DELETE | `/api/orcamentos/{id}/servicos/{itemId}` | Remove serviço |
| PUT | `/api/orcamentos/{id}/condicoes` | BDI, margem mínima e validade |
| PATCH | `/api/orcamentos/{id}/status` | Muda o status |
| GET | `/api/orcamentos/{id}/materiais` | Lista de compras |
| GET | `/api/orcamentos/{id}/proposta.pdf` | Proposta em PDF |
| POST | `/api/orcamentos/{id}/custos` | Lança custo real |
| GET | `/api/orcamentos/{id}/margem` | Análise de margem |
| GET/PUT | `/api/catalogo/materiais[/{codigo}]` | Catálogo de materiais |
| GET/PUT | `/api/catalogo/mao-de-obra[/{codigo}]` | Catálogo de mão de obra |
| GET | `/api/servicos` | Tipos de serviço e parâmetros |

## Fluxo de trabalho no Git

- `main` sempre estável; trabalho novo em `feature/<nome>`, correções em `fix/<nome>`
- Commits no padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/) (`feat:`, `fix:`, `docs:`, `test:`, `refactor:`)
- Pull request só entra com o CI verde (`mvn verify`)

## Aviso

Os coeficientes de consumo e os preços do catálogo são **referências para estudo**. Antes de usar
em proposta real, confira com as composições da sua empresa ou do SINAPI e com os preços da sua região.
