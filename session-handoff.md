# Session Handoff — auth-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-15

## Objetivo atual

`feat-001`..`feat-015` `done`. `feat-015` (orquestração de provisionamento de tenant em
bets-service/stats-service) fechada nesta sessão — código já mergeado antes, faltava só a
verificação real de produção. Ver `progress.md`.

## Concluído nesta sessão (2026-09-15)

- [x] `feat-015` fechada (story SV-382, subtasks SV-383/384). Verificação real contra o k3s de
      produção feita junto com `infra/feat-006` (mesma mudança cross-repo): env vars aplicadas no
      servidor Debian real, pod reiniciado, 1 chamada admin confirmando
      `downstreamProvisioningFailures: []`.

## Bloqueios / Riscos

Nenhum.

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (deve sair `0`) — **Docker Desktop precisa estar rodando** antes (testes de
   integração usam Testcontainers/Postgres real); se `init.sh` falhar com "Previous attempts to
   find a Docker environment failed", é isso, não regressão de código.
2. Nenhuma feature elegível neste harness até surgir novo achado cross-service.
