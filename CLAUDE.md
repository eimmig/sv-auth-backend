# CLAUDE.md — auth-service

Serviço de cadastro e autenticação. Java 25 + Spring Boot 4.x. Parte do harness multinível do
monorepo — leia `../../CLAUDE.md` (raiz) para invariantes cross-service antes deste arquivo, e
`../../docs/services/auth-service.md` para o desenho completo (modelo de dados, RF cobertos).
Arquitetura interna, build tool, testes e formato de API são normativos e já decididos em
`../../docs/CONVENTIONS.md`, `../../docs/TESTING.md` e `../../docs/API-CONTRACTS.md` — leia-os
antes de `feat-001`.

## Fluxo de início de sessão (Startup Workflow)

1. Confirme o diretório de trabalho (`pwd`) — deve ser `services/auth-service`.
2. Leia `../../CLAUDE.md` e `../../docs/services/auth-service.md`.
3. Rode `./init.sh` para verificar build/testes deste serviço.
4. Leia `feature_list.json` (deste serviço) para a próxima feature granular.
5. Leia `progress.md` (deste serviço).

## Regras específicas deste serviço

- **Uma feature por vez (One feature at a time)**: escolha exatamente uma feature `not-started`
  de `feature_list.json` cujas dependências já estejam `done`.
- **Escopo restrito (stay in scope)**: não edite código de outro serviço a partir desta pasta.
- Banco Postgres próprio, nome sugerido `auth` (Database per Service — ver raiz),
  **schema-per-tenant** (`tenant_<slug>`, migração lazy — ver `../../docs/CONVENTIONS.md` seção
  "Migrations"). `USER` vive dentro do schema do tenant, **sem** coluna `tenantId` — ver
  `../../docs/DATA-MODEL.md` e `../../docs/DECISIONS-LOG.md` 2026-08-02.
- Autenticação via token **PASETO**, não JWT. Este é o único serviço que emite/valida o token de
  fato — os demais confiam nos headers `X-User-Id`/`X-Tenant-Id` injetados pelo API Gateway (ver
  `../../docs/API-CONTRACTS.md`, seção "Confiança entre serviços"). Os dois **não são o mesmo
  valor**: um tenant é uma organização, pode ter vários usuários (`USER.role`
  `admin`/`member`) — não confundir com o modelo antigo (tenant = usuário).
- **Tenant é criado por uma rota administrativa restrita ao operador da plataforma** (não há
  autocadastro público de organização nem de usuário) — cria o schema, roda as migrations, e o
  primeiro usuário (`admin@<slug>`, `role = admin`, senha **aleatória** + `mustChangePassword =
  true`). Dentro do tenant, só esse admin cria novos usuários (`role = member`) — checagem de
  autorização por role é lógica de negócio nova, não só a coluna. Autenticação da rota admin via
  header **`X-Admin-Api-Key`** (segredo estático, ver `../../docs/API-CONTRACTS.md`).
  Orquestração: **3 chamadas manuais separadas** do operador (este serviço primeiro, depois
  `bets-service`, depois `stats-service`) — este serviço **não** chama os outros dois em código
  (decisão de 2026-08-02, ver `../../docs/DECISIONS-LOG.md` item 3).
- **`GET /api/v1/telegram-accounts/{telegramUserId}`**: consulta o diretório `TELEGRAM_LINK`
  (schema `public`, fora de qualquer schema de tenant — ver `../../docs/DATA-MODEL.md`). Lookup
  direto, sem ambiguidade de schema (resolvido em 2026-08-02, ver `../../docs/DECISIONS-LOG.md`
  item 15). O endpoint de geração de código de vínculo grava em `PENDING_TELEGRAM_LINK` (mesmo
  schema `public`) além do schema do tenant; a confirmação faz upsert em `TELEGRAM_LINK` na
  mesma transação que cria/atualiza `TELEGRAM_ACCOUNT` no schema do tenant — é uma transação
  local comum (mesmo banco Postgres), não uma transação distribuída.
- **Maven** (não Gradle) e **arquitetura hexagonal** (`domain/`, `application/`, `adapter/`) —
  decisões já tomadas em `../../docs/CONVENTIONS.md`, não reabrir.
- Erros de API em `application/problem+json` (RFC 7807) — ver `../../docs/API-CONTRACTS.md`,
  com `title`/`detail` localizados por `Accept-Language` (`pt-BR`/`en-US`/`es` sempre em
  sincronia, ver `../../docs/CONVENTIONS.md` seção "Internacionalização (i18n)") — `type`
  continua um slug fixo em inglês.
- Não modele `BETTING_HOUSE`, `BET` ou qualquer entidade de apostas aqui — pertence a
  `services/bets-service`.
- Este serviço é dono do vínculo `TELEGRAM_ACCOUNT` mesmo não recebendo mensagens do bot
  diretamente — ver `feat-006` e `../../docs/services/auth-service.md` seção "Vínculo de conta
  Telegram" (bloqueada, ver bullet acima). O lookup `GET /api/v1/telegram-accounts/{telegramUserId}`
  é interno: só o `services/api-gateway` deve alcançá-lo, nunca exposto sem passar por ele.
- Não implemente um filtro/middleware para os outros serviços Java validarem o token PASETO
  diretamente — essa responsabilidade é exclusiva de `services/api-gateway` (`epic-008`), ver
  `../../docs/API-CONTRACTS.md`.
- **CI/CD (`feat-007`)**: pipeline em `.github/workflows/ci.yml`, **dentro deste repositório**
  (este serviço é seu próprio repositório Git, não um monorepo — ver
  `../../docs/DECISIONS-LOG.md` "Topologia") — changelog, i18n, build, testes, SonarCloud.
  Scripts de validação em `.github/scripts/` (duplicados aqui, não compartilhados com os outros
  serviços). Ver `../../docs/CI-CD.md`. Toda feature adiciona uma entrada em `CHANGELOG.md`
  deste serviço (verificado automaticamente pelo CI quando este repositório existir no GitHub).
- **Skills de agente prioritárias**: `Plan Reviewer` antes de codificar, `Delivery Reviewer` +
  `Test Suite Auditor` + `Persistence Auditor` (banco próprio, schema-per-tenant) antes de
  marcar `done` (claude-code-skills) — mapeamento completo em `../../docs/AGENT-SKILLS.md`.
  Instaladas em 2026-08-02 (escopo `user`), ver `../../docs/DECISIONS-LOG.md`.

## Definição de pronto (Definition of Done)

Uma feature deste serviço só está `done` quando (done only when):

> **Antes de começar** (não é item de `done`, é pré-requisito de `in-progress`): o campo
> `plan_review` daquela feature em `feature_list.json` precisa estar preenchido com o
> resultado do `Plan Reviewer` — ver `CLAUDE.md` da raiz, seção "Regras de trabalho".


- [ ] Implementada e rodando via `./init.sh` sem erro (`mvn verify`, gate de cobertura incluso).
- [ ] Testes seguindo `../../docs/TESTING.md` (unitários em `domain`/`application`, integração
      com Testcontainers em `adapter/out/persistence`, i18n: `title`/`detail` mudam por
      `Accept-Language` em pelo menos um teste).
- [ ] `Delivery Reviewer`, `Test Suite Auditor` e `Persistence Auditor` rodados contra a feature
      (ver `../../docs/AGENT-SKILLS.md`).
- [ ] Vault (`docs/`) revisado (item de checklist obrigatório na última subtask, ver `CLAUDE.md`
      da raiz seção "Feature se quebra em subtasks"): alguma descoberta desta feature sem nota
      correspondente foi atualizada/criada no mesmo commit.
- [ ] `CHANGELOG.md` deste serviço tem uma entrada em `[Unreleased]` descrevendo a mudança.
- [ ] `feature_list.json` atualizado com status e evidência.
- [ ] `../../feature_list.json` (raiz) atualizado se este foi o marco que fecha `epic-002`.

## Fim de sessão (End of Session)

Antes de encerrar (before ending a session): atualize `progress.md` deste serviço, atualize
`feature_list.json`, e deixe `./init.sh` passando (clean, restartable state) — stay in scope:
não toque em outros serviços nesta sessão a menos que a feature seja explicitamente
cross-service.

## Verificação

```bash
./init.sh
```
