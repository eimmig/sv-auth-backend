# Session Handoff — auth-service

## Current Objective

- Goal: `feat-001` (project setup) — done. `feat-002` (USER/TELEGRAM_ACCOUNT entities) next.
- Current status: Spring Boot 4.1.1 skeleton, tenant schema provisioning, i18n, health checks,
  JaCoCo gate all shipped and merged into `develop`.
- Branch / commit: `develop` @ `d000c47` (merge of `feature/SV-10`, story SV-10, 9 subtasks).

## Completed This Session

- [x] `feat-001` fully implemented across 9 subtasks (SV-11..SV-19) — see `progress.md` and the
      `evidence` field of `feat-001` in `feature_list.json` for full detail.
- [x] `/code-review` run before every subtask PR (new project-wide rule, adopted this session) —
      caught and fixed 4 real issues before merge: RFC 7807 error format, a Flyway hot-path
      caching gap, a locale-resolver fallback gap, and an MDC-placement gap.
- [x] `Delivery Reviewer` (PASS, no findings), `Test Suite Auditor` (found and closed a domain
      coverage gap — `TenantSchemaNameTest`), `Persistence Auditor` (found a hot-path query
      issue; the first fix introduced a security-adjacent regression that `/code-review` caught
      before merge — see `progress.md` "Decisões tomadas").

## Verification Evidence

| Check | Command | Result | Notes |
|---|---|---|---|
| Build/test | `./init.sh` | exit 0 | 22 tests, 0 failures, JaCoCo 80% gate passed, Docker required (Testcontainers). |
| CI | GitHub Actions, all 10 PRs | green | Includes SonarCloud on the final story→develop PR. |
| Secrets scan | GitGuardian | historical false positive | See "Blockers / Risks" below — fixed in HEAD, flagged commits remain in history. |

## Files Changed

- Full project skeleton: `pom.xml`, `src/main/**`, `src/test/**`, `application.yml`,
  `.env.example`, `CHANGELOG.md`, `.github/workflows/ci.yml`, `feature_list.json`.

## Decisions Made

- See `progress.md` "Decisões tomadas" — `spring-dotenv` not used, `@RestControllerAdvice`
  skeleton not created (YAGNI, no real exception to map yet), `migrateIfPending` never caches
  `exists()` (security-relevant check, always runs).

## Blockers / Risks

- GitGuardian (GitHub App check, not part of this harness's own CI) flagged an example password
  in `.env.example` — only on the final story→develop PR, since it only scans `pull_request`
  events and no earlier subtask PR had touched that file after it existed with that value. Fixed
  in `feat-001.9` (placeholder changed to `CHANGE_ME`), but the flagged commits remain in git
  history (not rewritten — rewriting published history over a false positive isn't worth the
  risk). Dashboard still shows the finding as "Triggered" against those old commits; no action
  needed unless GitGuardian is configured to block merges in the future.

## Next Session Startup

1. Read `../../CLAUDE.md` and `../../docs/services/auth-service.md`.
2. Read this directory's `CLAUDE.md`, `feature_list.json`, `progress.md`.
3. Run `./init.sh` (needs Docker running for Testcontainers).

## Recommended Next Step

- Start `feat-002` (USER/TELEGRAM_ACCOUNT entities + first real Flyway migrations). Plan Reviewer
  first, per `CLAUDE.md` (raiz) — this is the first feature to actually populate
  `src/main/resources/db/migration/` and to need Lombok in `adapter/out/persistence/`.
