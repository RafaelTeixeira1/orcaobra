# Registro do estágio

Esta pasta transforma o trabalho feito no OrçaObra no **Relatório Diário de Atividades** do estágio
(modelo oficial do IF Goiano – Campus Ceres, em `modelo/`).

## Rotina de cada dia

1. Trabalhe no projeto e faça commits pequenos e descritivos (`feat: ...`, `fix: ...`, `docs: ...`).
2. No fim do dia, gere a foto de registro a partir do Git:

   ```bash
   python estagio/gerar_evidencia.py 2026-11-25 --extra "mvn verify: BUILD SUCCESS"
   ```

   Se preferir, use um print da tela do sistema ou da IDE e salve em `fotos/`.
3. Acrescente o dia em `registro.yaml` (data, horas, números das atividades do plano, descrição e fotos).
4. Gere o relatório:

   ```bash
   pip install python-docx pyyaml pillow
   python estagio/gerar_relatorio.py
   ```

   O arquivo sai em `estagio/saida/Relatorio-Diario-Estagio.docx` (e `.pdf`, se houver LibreOffice).

## Atividades do plano de estágio

| Nº | Atividade | Exemplos no OrçaObra |
|---|---|---|
| 1 | Levantamento e análise de requisitos | `docs/requisitos.md`, novas regras de orçamento |
| 2 | Desenvolvimento e manutenção de sistemas web | API REST, front-end, novas calculadoras |
| 3 | Modelagem e manutenção de bancos de dados | migrações Flyway, modelo JPA, PostgreSQL |
| 4 | Testes, identificação e correção de erros | JUnit, MockMvc, ArchUnit, correções |
| 5 | Versionamento e organização do processo | Git, branches, pull requests, GitHub Actions |
| 6 | Documentação | README, `docs/arquitetura.md`, Swagger |
| 7 | Suporte técnico e resolução de problemas | configuração de ambiente, Docker, deploy |

Registre só o que foi feito de fato em cada dia: o relatório é assinado por você e pelo supervisor.
