# Log de Progresso — auth-service

## Estado Atual (Current State)

**Última atualização:** 2026-09-03
**Feature ativa:** nenhuma (`feat-001` `done`, `feat-002` liberada)

## Status

### O que está pronto

- [x] Harness deste serviço criado.
- [x] **`feat-001` (Setup do projeto) — `done` em 2026-09-03.** Spring Boot 4.1.1 (Java 25,
      Maven), layout hexagonal, groupId `com.stakevault.betting`. Conexão Postgres com profiles
      dev/test/prod. Provisionamento de schema de tenant + migração lazy por requisição
      (`ProvisionTenantSchemaUseCase`/`TenantSchemaFilter`, erros em RFC 7807, `tenantId` no
      MDC). Gate JaCoCo 80% vinculado a `verify`. i18n (`MessageSource` + `LocaleResolver`
      restrito aos 3 locales). Health checks do Actuator (`/actuator/health`,
      `/actuator/health/readiness` com checagem do Postgres). `.env.example` + logging JSON
      estruturado (`logging.structured.format.console: ecs`). 9 subtasks (SV-11..SV-19, a última
      descoberta só no gate final — falso positivo do GitGuardian). Evidência completa em
      `feature_list.json`.

### Em andamento

- Nenhuma feature iniciada.

### Próximos passos (Next Steps)

1. `feat-002` — entidades `USER` e `TELEGRAM_ACCOUNT` (migrations Flyway reais — até aqui
   `db/migration` está vazio de propósito). Ver `../../docs/DATA-MODEL.md`.

## Bloqueios / Riscos

- Nenhum bloqueio real. Achado de processo (não bloqueia `feat-002`): GitGuardian (GitHub App,
  fora do `ci.yml` deste harness) só escaneia eventos de `pull_request` — um valor de exemplo em
  `.env.example` só foi flagado no PR final (`feature/SV-10` → `develop`), não em nenhuma das 8
  PRs de subtask que vieram antes. Corrigido (`feat-001.9`), mas o achado ainda aparece marcado
  como "Triggered" no dashboard do GitGuardian para os commits antigos do histórico (não
  reescrito — reescrever histórico publicado por um falso positivo não compensa o risco). Vale
  revisar `.env.example` de cada novo serviço Java com esse mesmo cuidado (placeholder sem
  formato de senha real, ex. `CHANGE_ME`) antes da primeira PR que o toque.

## Decisões tomadas

- Build tool: **Maven**. Arquitetura: **hexagonal** (domain/application/adapter). Ambas
  decididas em `../../docs/CONVENTIONS.md`, não específicas desta sessão.
- `spring-dotenv` (citado no plano original de `feat-001.2`) **não foi usado** — variáveis de
  ambiente diretas (`${DB_HOST:localhost}` etc.) já bastam, e a lib não tem atividade recente
  compatível com Spring Boot 4.1/Spring 7 confirmada.
- `@RestControllerAdvice` (citado no plano original de `feat-001.5`) **não foi criado** —
  `feat-001` não tem nenhuma exceção de negócio real para mapear ainda; a classe nasceria vazia.
  `TenantSchemaFilter` resolve seus dois erros (`invalid-tenant-id`, `tenant-not-found`)
  diretamente. Revisitar quando `feat-004` trouxer a primeira exceção de domínio real.
- `ProvisionTenantSchemaUseCase.migrateIfPending` **nunca cacheia o resultado de `exists()`** —
  só o `Flyway.migrate()` (custo, já cacheado em `JdbcFlywayTenantSchemaGateway`) é pulável.
  Decisão tomada depois de reverter uma versão que cacheava os dois: um schema derrubado em
  runtime continuaria autorizando requisições até o processo reiniciar. Ver `feature_list.json`
  (evidência de `feat-001`) para o achado completo do Persistence Auditor.

## Arquivos modificados nesta sessão

- Todo o esqueleto do projeto (`pom.xml`, `src/main`, `src/test`), `application.yml`,
  `.env.example`, `CHANGELOG.md`, `.github/workflows/ci.yml` (3 correções de guarda por
  marcador), `feature_list.json` — ver commits em `feature/SV-10` (mergeada em `develop`,
  `d000c47`).

## Evidência de conclusão

- Ver campo `evidence` de `feat-001` em `feature_list.json` (objeto estruturado com 4 seções:
  verificação real executada, divergência do plano original, defeitos encontrados antes de
  causar dano, skills e ferramentas).

## Notas para a próxima sessão

- `feat-002` (entidades `USER`/`TELEGRAM_ACCOUNT`) é a primeira feature que realmente usa
  `src/main/resources/db/migration/` — hoje vazio (Flyway roda com zero migrations, confirmado
  nos testes de `feat-001.3`). Primeira migration real também é o primeiro caso de uso real de
  `ProvisionTenantSchemaUseCase.ensureSchemaExists` fora de teste.
- Lombok (permitido só em `adapter/out/persistence/`, ver `../../docs/CONVENTIONS.md`) ainda não
  foi adicionado ao `pom.xml` — `feat-002` é quem precisa dele pela primeira vez.
