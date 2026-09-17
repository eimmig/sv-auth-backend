# Session Handoff — auth-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-17

## Objetivo atual

`feat-001`..`feat-018` `done`. Backlog deste serviço esgotado. Fecha `epic-029` da raiz.

## Concluído nesta sessão (2026-09-17)

- [x] **`feat-018` fechada** (`POST /api/v1/auth/change-password` — usuário logado troca a
      própria senha, `currentPassword`+`newPassword`, `204` sem corpo, zera `mustChangePassword`
      ao trocar com sucesso). Decisão do usuário via `AskUserQuestion` antes do plan review:
      `mustChangePassword` continua sem bloqueio real de outras rotas — fecha o item aberto do
      `DECISIONS-LOG` de 2026-09-04. `Plan Reviewer` corrigiu 1 achado MAJOR: exceção dedicada
      (`CurrentPasswordMismatchException`) em vez de reusar `InvalidCredentialsException` do
      login (texto localizado enganoso — menciona tenant/e-mail). Bug real de produção pego pelo
      próprio teste de integração: `UserJpaEntity.applyUpdate()` (`feat-017`) só aplicava
      `name`/`role`, descartando silenciosamente `passwordHash`/`mustChangePassword` — corrigido
      alargando para os 4 campos, sem regressão em `feat-017`. Story SV-510 (subtasks
      SV-511/512/513), PRs #69/#70/#71 → `feature/SV-510`, PR #72 → `develop`, CI+SonarCloud
      verdes (SonarCloud reprovou 1x por 2 MINOR reais, corrigidos no mesmo PR).
      `Delivery Reviewer`/`Test Suite Auditor`/`Persistence Auditor` — todos PASS. `./init.sh`
      verde. Detalhe completo em `progress.md` e no campo `evidence` de `feat-018` em
      `feature_list.json`.

## Bloqueios / Riscos

Nenhum.

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (deve sair `0`) — **Docker Desktop precisa estar rodando** antes (testes de
   integração usam Testcontainers/Postgres real).
2. Nenhuma feature elegível neste harness até surgir novo achado cross-service ou pedido do
   usuário — verificar `../../feature_list.json` (raiz) por epics novos dependentes de
   `epic-002`/`epic-029`.
