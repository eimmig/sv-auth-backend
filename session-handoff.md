# Session Handoff — auth-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-16

## Objetivo atual

`feat-001`..`feat-017` `done`. Backlog deste serviço esgotado.

## Concluído nesta sessão (2026-09-16)

- [x] **`feat-017` fechada** (`PATCH /api/v1/users/{id}` — atualizar `name`/`role` de um usuário
      do tenant, restrito a `role=admin`). `Plan Reviewer` corrigiu 2 achados MAJOR antes de
      codificar: `@Setter` amplo do Lombok em `UserJpaEntity` (contrariava `docs/CONVENTIONS.md`)
      e ausência de trava contra rebaixar o último admin do tenant (risco de lockout permanente —
      não há rota de promoção `member`→`admin` em nenhuma feature do backlog). Ambos corrigidos:
      `UserJpaEntity.applyUpdate(name, role)` dedicado (setter removido), e
      `LastAdminCannotBeDemotedException` (409) quando a transição `ADMIN`→`MEMBER` deixaria o
      tenant sem nenhum admin. Story SV-486 (subtasks SV-487/488/489, `017.1`+`017.2` bundladas no
      mesmo commit), PR #67 → `develop`, CI+SonarCloud+GitGuardian verdes, merged. `./init.sh`
      verde (196 testes). `Delivery Reviewer`/`Test Suite Auditor`/`Persistence Auditor`
      rodados como self-review de passe único — sem achado bloqueante. Detalhe completo em
      `progress.md` e no campo `evidence` de `feat-017` em `feature_list.json`.

## Bloqueios / Riscos

Nenhum. (Nota: durante o fechamento desta sessão, um `git pull`/`fetch` local falhou uma vez por
instabilidade de rede transitória — não regressão, resolvido no retry, `develop` local já
sincronizado com `origin/develop` incluindo o merge do PR #67.)

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (deve sair `0`) — **Docker Desktop precisa estar rodando** antes (testes de
   integração usam Testcontainers/Postgres real).
2. Nenhuma feature elegível neste harness até surgir novo achado cross-service ou pedido do
   usuário.
