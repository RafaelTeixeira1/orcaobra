# Levantamento de requisitos — OrçaObra

## 1. Contexto

Engenheiros civis e profissionais da construção costumam orçar casas em planilhas soltas: o
quantitativo de materiais é refeito a cada obra, o BDI é aplicado "no olho", a proposta enviada ao
cliente é montada à mão e, depois que a obra começa, ninguém sabe se o lucro previsto está se
confirmando. O OrçaObra resolve esse ciclo inteiro: **calcular → precificar → propor → acompanhar**.

## 2. Atores

| Ator | Descrição |
|---|---|
| Orçamentista (engenheiro, mestre de obras, construtor) | Cria orçamentos, ajusta preços e BDI, gera propostas e lança custos reais. |
| Cliente final | Recebe a proposta em PDF. Não acessa o sistema. |

## 3. Requisitos funcionais

| Código | Requisito | Prioridade |
|---|---|---|
| RF01 | Cadastrar orçamento com dados do cliente e da obra (descrição, endereço, cidade, área construída). | Alta |
| RF02 | Adicionar serviços ao orçamento informando tipo e quantidade (m² ou m³) e parâmetros opcionais (espessura, demãos, perda). | Alta |
| RF03 | Calcular automaticamente os materiais (com perdas) e as horas de mão de obra de cada serviço. | Alta |
| RF04 | Precificar materiais e mão de obra a partir de um catálogo de preços editável. | Alta |
| RF05 | Congelar os preços no orçamento: alterar o catálogo não muda orçamentos já calculados. | Alta |
| RF06 | Aplicar BDI com composição detalhada (administração central, seguro, risco, despesas financeiras, lucro, impostos) segundo a fórmula do TCU. | Alta |
| RF07 | Gerar lista de compras consolidada, arredondando sacos e unidades para cima. | Média |
| RF08 | Gerar proposta comercial profissional em PDF, sem expor custos internos, BDI ou margem. | Alta |
| RF09 | Controlar o ciclo de vida do orçamento: rascunho → enviado → aprovado/recusado → em execução → concluído (ou cancelado). | Alta |
| RF10 | Lançar custos reais da obra (material, mão de obra, outros) durante a execução. | Alta |
| RF11 | Exibir margem prevista, margem projetada e previsto × realizado por categoria. | Alta |
| RF12 | Alertar quando a margem projetada cair abaixo da margem mínima definida pelo orçamentista. | Alta |
| RF13 | Expor todas as funções por API REST documentada (OpenAPI/Swagger). | Média |

## 4. Requisitos não funcionais

| Código | Requisito |
|---|---|
| RNF01 | Back-end em Java 21 com Spring Boot 3. |
| RNF02 | Regras de negócio isoladas de framework (arquitetura hexagonal), testáveis sem banco ou servidor. |
| RNF03 | Valores monetários com `BigDecimal` e arredondamento bancário; nunca `double`. |
| RNF04 | Esquema de banco versionado com migrações (Flyway); H2 em desenvolvimento e PostgreSQL em produção. |
| RNF05 | Erros da API no padrão RFC 7807 (Problem Details), com mensagens em português. |
| RNF06 | Build e testes automatizados em integração contínua (GitHub Actions). |
| RNF07 | Interface web responsiva, utilizável no celular na obra. |

## 5. Regras de negócio

- **RN01** — Itens, BDI e validade só podem ser alterados com o orçamento em **rascunho**.
- **RN02** — Um orçamento só pode ser enviado se tiver ao menos um serviço.
- **RN03** — Custos reais só podem ser lançados com a obra **em execução**.
- **RN04** — Proposta não pode ser gerada para orçamento recusado ou cancelado.
- **RN05** — Preço de venda = custo direto × (1 + BDI).
- **RN06** — Margem = (preço − impostos − custos indiretos − custo direto) ÷ preço.
- **RN07** — Custo projetado de cada categoria = maior valor entre o previsto e o já realizado.
- **RN08** — O alerta de margem é disparado uma vez, quando a margem projetada **cruza** o mínimo.
- **RN09** — Na proposta, a soma dos itens deve ser exatamente igual ao valor total (o último item absorve a diferença de centavos).

## 6. Casos de uso

1. **Criar orçamento** (RF01)
2. **Adicionar / remover serviço** (RF02–RF05)
3. **Definir BDI e condições** (RF06)
4. **Consultar lista de materiais** (RF07)
5. **Gerar proposta em PDF** (RF08)
6. **Alterar status** (RF09)
7. **Lançar custo realizado** e **analisar margem** (RF10–RF12)
8. **Manter catálogo de preços** (RF04)

## 7. Coeficientes de consumo adotados

Valores de referência usuais em composições de custo de edificações (ordem de grandeza compatível com
o SINAPI). Ficam como constantes em cada calculadora e podem ser calibrados pela equipe.

| Serviço | Material / mão de obra | Consumo |
|---|---|---|
| Concreto 1:2:3 (por m³) | Cimento / areia / brita | 7 sc / 0,56 m³ / 0,84 m³ (+5% de perda) |
| | Pedreiro / servente | 2 h / 6 h |
| Alvenaria bloco 9x19x19 (por m²) | Blocos | 25 un (+5% de perda) |
| | Cimento / areia | 0,06 sc / 0,015 m³ |
| | Pedreiro / servente | 0,8 h / 0,8 h |
| Reboco (por m², e = 2 cm) | Argamassa | área × espessura × 1,10; 3,6 sc e 1,10 m³ de areia por m³ + chapisco |
| | Pedreiro / servente | 0,7 h / 0,7 h |
| Contrapiso 1:4 (por m², e = 5 cm) | Argamassa | área × espessura × 1,10; 7 sc e 1,15 m³ de areia por m³ |
| | Pedreiro / servente | 0,35 h / 0,45 h |
| Piso cerâmico (por m²) | Piso / argamassa AC-I / rejunte | 1,10 m² / 0,25 sc / 0,3 kg |
| | Pedreiro / servente | 0,6 h / 0,3 h |
| Pintura acrílica (por m²) | Selador / tinta | 1/12 L / demãos ÷ 10 L |
| | Pintor | 0,08 h + 0,15 h por demão |
