# Session Handoff — auth-service

> Estado atual, não histórico. O diário cronológico é o `progress.md` — este arquivo é reescrito
> a cada sessão para responder "o que a próxima sessão precisa saber agora".

**Última atualização:** 2026-09-10

## Objetivo atual

`feat-001`..`feat-013` `done`. `feat-012` (claim `role` no token PASETO) e `feat-013` (correção
de casing do mesmo claim) fechadas nesta sessão — achados de `bets-service epic-013`, ver
`progress.md`.

## Concluído nesta sessão (2026-09-10)

- [x] `feat-012` fechada (story SV-311, PRs #51/#52). `AccessTokenIssuer.issue` ganha `Role`,
      `PasetoClaims` ganha campo `role`. Libera `api-gateway` (feature irmã, extrair+injetar
      `X-User-Role`) e `bets-service epic-013` (`PATCH /api/v1/settings`).
- [x] `feat-013` fechada (story SV-315, PRs #53/#54) — correção real do casing do claim `role`
      (lowercase, não `role.name()` cru), achado do Plan Reviewer de `bets-service epic-013`.

## Bloqueios / Riscos

Nenhum.

## Próxima sessão — por onde começar

1. Rodar `./init.sh` (deve sair `0`).
2. Nenhuma feature elegível neste harness até surgir novo achado cross-service.
3. Fora deste serviço: `api-gateway` precisa da feature irmã (extrair `role` do token, injetar
   `X-User-Role`) antes de `bets-service epic-013` poder confiar no header.
