# Session Handoff — auth-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-15

## Objetivo atual

`feat-001`..`feat-016` `done`. Backlog deste serviço esgotado.

## Concluído nesta sessão (2026-09-15)

- [x] `feat-015` fechada (story SV-382, subtasks SV-383/384). Verificação real contra o k3s de
      produção feita junto com `infra/feat-006` (mesma mudança cross-repo): env vars aplicadas no
      servidor Debian real, pod reiniciado, 1 chamada admin confirmando
      `downstreamProvisioningFailures: []`.
- [x] **`feat-016` fechada** (CD automático — job `deploy` em `ci.yml`, `kubectl rollout restart
      deployment/auth-service` contra `KUBE_CONFIG`/`ci-deployer` de `infra/feat-007`). Quarta
      aplicação idêntica do padrão de `epic-028` já revisado nesta sessão (`bets-service
      feat-018`/`stats-service feat-019`/`api-gateway feat-014`) — única diferença o nome do
      `Deployment`. Story SV-432, subtasks SV-433/SV-434, PRs #63/#64/#65, CI+SonarCloud verdes.
      `Delivery Reviewer`: PASS. Fechamento em 2 disparos de `--sync-status` (subtask done sozinha
      → `Review`; feature done em edição separada → `Done`), padrão correto após o erro cometido
      nas 2 primeiras features de `epic-028` (ver `services/bets-service`/`stats-service
      progress.md`). Disparo real do job adiado (mesma decisão das 3 features anteriores).

## Bloqueios / Riscos

Nenhum.

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (deve sair `0`) — **Docker Desktop precisa estar rodando** antes (testes de
   integração usam Testcontainers/Postgres real); se `init.sh` falhar com "Previous attempts to
   find a Docker environment failed", é isso, não regressão de código.
2. Nenhuma feature elegível neste harness até surgir novo achado cross-service.
