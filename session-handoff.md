# Session Handoff — auth-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-23

## Objetivo atual

`feat-001`..`feat-019` `done`. Backlog deste serviço esgotado.

## Concluído nesta sessão (2026-09-23)

- [x] **`feat-019` fechada** (Reformulação de marca StakeVault -> Arka, continuação do `epic-032`
      da raiz - 4º harness na ordem sugerida, primeiro dos 4 serviços Java). Único ponto real de
      marca: `pom.xml` linha 15 (`<description>`) - GroupId `com.stakevault.betting` fora de
      escopo (identificador técnico, `docs/DECISIONS-LOG.md` raiz 2026-09-23). Plan Reviewer
      (READY, cobrindo os 4 serviços Java de uma vez) + Delivery Reviewer (PASS) passe próprio.
      Story SV-549, PRs #73-75, CI+SonarCloud verdes.
- [x] **Achado de ambiente, não desta mudança**: 8 processos `java.exe` órfãos de outro teste do
      usuário travavam os jars antigos em `target/` dos 4 serviços Java, impedindo o `repackage`
      do `mvn verify` local no Windows. Usuário autorizou pular o build local (`mvn test` rodado
      em vez disso, EXIT=0) - o CI (Linux) roda `mvn verify` completo sem esse lock, fechando o
      gap real.

## Bloqueios / Riscos

Nenhum. `epic-032` (raiz) fechou os 4 serviços Java nesta sessão - próximo harness na ordem
sugerida: `infra/` (último).

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (deve sair `0`) — **Docker Desktop precisa estar rodando** antes (testes de
   integração usam Testcontainers/Postgres real).
2. Nenhuma feature elegível neste harness até surgir novo achado cross-service ou pedido do
   usuário — verificar `../../feature_list.json` (raiz) por epics novos dependentes de
   `epic-002`/`epic-029`.
