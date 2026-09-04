# Log de Progresso — auth-service

## Estado Atual (Current State)

**Última atualização:** 2026-09-04
**Feature ativa:** nenhuma (`feat-006` `done`, `feat-007` liberada)

## Status

### O que está pronto

- [x] Harness deste serviço criado.
- [x] **`feat-001` (Setup do projeto) — `done` em 2026-09-03.** Spring Boot 4.1.1 (Java 25,
      Maven), layout hexagonal, groupId `com.stakevault.betting`. Conexão Postgres com profiles
      dev/test/prod. Provisionamento de schema de tenant + migração lazy por requisição
      (`ProvisionTenantSchemaUseCase`/`TenantSchemaFilter`, erros em RFC 7807, `tenantId` no
      MDC). Gate JaCoCo 80% vinculado a `verify`. i18n (`MessageSource` + `LocaleResolver`
      restrito aos 3 locales). Health checks do Actuator. `.env.example` + logging JSON
      estruturado. Evidência completa em `feature_list.json`.
- [x] **`feat-002` (Entidades USER e TELEGRAM_ACCOUNT) — `done` em 2026-09-04.** Primeira
      migration real (`users`/`telegram_accounts`), primeiro mapeamento JPA do serviço, e
      multi-tenancy do Hibernate por schema (`CurrentTenantIdentifierResolver` +
      `MultiTenantConnectionProvider`, chaves confirmadas via `javap` contra o jar instalado).
      8 subtasks (SV-23..SV-30 — a última, reabertura pós-merge para corrigir 27 apontamentos
      reais do SonarCloud ignorados na PR original e travar o gate de verdade, ver "Bloqueios /
      Riscos"). Evidência completa em `feature_list.json`.
- [x] **`feat-003` (Provisionamento de tenant — rota admin) — `done` em 2026-09-04.** Primeiro
      endpoint HTTP real do serviço (`POST /api/v1/admin/tenants`), primeira implementação de
      verdade do padrão `@RestControllerAdvice`/`ProblemDetail` documentado desde `feat-001` mas
      nunca exercitado. 7 subtasks (SV-32..SV-38). Achado crítico de infraestrutura corrigido:
      `MessageSourceAutoConfiguration` do Spring Boot nunca ativava sem `messages.properties`
      base (sem sufixo de locale) — todo `getMessage()` real da aplicação lançava
      `NoSuchMessageException` silenciosamente desde `feat-001.5`. Evidência completa em
      `feature_list.json`.

- [x] **`feat-004` (RF01, criação de usuário dentro do tenant) — `done` em 2026-09-04.**
      `POST /api/v1/users`, primeira rota de negócio real fora da rota admin de `feat-003`.
      Confia diretamente em `X-User-Id`/`X-Tenant-Id` (decisão tomada com o usuário — sem
      PASETO/`api-gateway` ainda, mesmo modelo de confiança que `bets-service`/`stats-service`
      vão usar). 4 subtasks (SV-40..43). Primeiro uso real de Bean Validation (`@Valid`) no
      serviço. Achado do Test Suite Auditor corrigido (isolamento cross-tenant só provado
      implicitamente — teste explícito adicionado). Convenção nova documentada em
      `docs/API-CONTRACTS.md`: 401 para header de identidade ausente/inválido, 400 para header
      de contexto de negócio ausente. Evidência completa em `feature_list.json`.
- [x] **`feat-005` (RF02, autenticação com token PASETO) — `done` em 2026-09-04.**
      `POST /api/v1/auth/login`, primeira feature de emissão de credencial do serviço. Token
      PASETO v4.local (`io.github.nbaars:paseto4j-version4:2024.3` — biblioteca confirmada via
      Maven Central/GitHub releases, mas o README/main branch divergia do release realmente
      publicado, corrigido via `javap` contra o jar instalado). 5 subtasks (SV-45..49, `feat-005.1..4`
      bundladas numa única branch/PR — interdependência descoberta na implementação, mesmo
      padrão de `feat-002.4/002.5`). `mustChangePassword=true` não bloqueia login (decisão do
      usuário — sem endpoint de troca de senha no backlog). Erro único `InvalidCredentialsException`
      (401 genérico) para as 4 causas de falha, evita enumeração de tenant/usuário. Achado real
      corrigido: `hash()` de descarte (mitigação de timing attack) vazava `IllegalArgumentException`
      crua para senha >72 bytes contra tenant/email inexistente — corrigido. Achado do Test Suite
      Auditor corrigido: isolamento cross-tenant no login só provado na persistência, teste
      explícito adicionado. Evidência completa em `feature_list.json`.
- [x] **`feat-006` (RF05 suporte, vínculo de conta Telegram) — `done` em 2026-09-04.**
      `POST /api/v1/telegram-links`, `POST /api/v1/telegram-accounts`, `GET
      /api/v1/telegram-accounts/{telegramUserId}`. Primeira feature com dado fora do
      schema-per-tenant (diretório global `TELEGRAM_LINK`/`PENDING_TELEGRAM_LINK`, schema
      `public`, migration eager) e primeiro `@Transactional` real do serviço, cruzando o schema
      do tenant e o `public` na mesma transação (`TelegramLinkConfirmationTransaction`). 6
      subtasks (SV-51..56, `feat-006.1..5` bundladas numa única branch/PR — interdependência
      descoberta na implementação, mesmo padrão de `feat-002.4/5`/`feat-005.1..4`).
      Reconfirmação de `telegramUserId` já vinculado a outro tenant sobrescreve o vínculo antigo
      (decisão do usuário). Achado do Persistence Auditor corrigido: a migration do schema
      `public` rodava via `ApplicationRunner`, que o Spring Boot só chama **depois** do servidor
      embutido já aceitar conexões — corrigido para `InitializingBean` (mesmo mecanismo do
      `FlywayMigrationInitializer` oficial). Achado do Test Suite Auditor corrigido: caminho 409
      só provado por mock, teste HTTP ponta a ponta adicionado. Evidência completa em
      `feature_list.json`.

- [x] **`feat-007` (Pipeline de CI - GitHub Actions + SonarCloud) — `done` em 2026-09-04.**
      Feature de fechamento formal, sem código/workflow novo: o pipeline já existia e já rodava
      de verdade em produção desde `epic-009` (setup) e `feat-001..006` (endurecimento
      incremental — guardas por marcador, `sonar.qualitygate.wait`, gate de zero issue/hotspot,
      branch protection). Único achado real: a `description` da própria feature ainda dizia "5
      passos" e citava o atalho `mvn sonar:sonar` — desatualizada desde que `feat-002.8`/SV-30
      adicionou o 6º passo (gate de zero issue). Corrigida para bater com `ci.yml` real. 2
      subtasks (SV-58/59). **Última feature do backlog atual de `auth-service` — fecha
      `epic-002` na raiz.** Evidência completa em `feature_list.json`.

### Em andamento

- Nenhuma feature iniciada.

### Próximos passos (Next Steps)

1. Backlog atual (`feat-001..007`) 100% `done`. Nenhuma feature liberada neste serviço até que a
   raiz abra um novo epic para `auth-service` (não há previsão no `feature_list.json` da raiz
   hoje — os epics restantes são de outros serviços).

## Bloqueios / Riscos

- Nenhum bloqueio real.
- **Achado real, corrigido em `feat-002.8`/SV-30**: a PR `feature/SV-22` → `develop` mergeou com
  27 issues do SonarCloud abertas (1 CRITICAL, 8 MAJOR, 18 MINOR) nunca revisadas — o gate padrão
  do SonarCloud (plano gratuito, sem gate customizável) só mede rating/cobertura/duplicação, não
  quantidade de issue nova, e o goal Maven não tinha `-Dsonar.qualitygate.wait=true` (o passo do
  CI "passava" sem nem esperar o resultado do gate). Corrigido: os 27 apontamentos reais + script
  novo (`validate-sonar-issues.py`, consulta `/issues/search` e `/hotspots/search` direto,
  paginado) + branch protection real no GitHub (`required_status_checks`, sem isso nenhum check
  do `ci.yml` bloqueava o botão de merge). Ver `docs/CI-CD.md` seção "SonarCloud: o Quality Gate
  padrão não bloqueia por issue nova" — `bets-service`/`stats-service` (mesma stack Java) só
  precisam replicar os arquivos, não redescobrir o problema.
- `UserRepository.save()`/`TelegramAccountRepository.save()` são **só de criação** (padrão
  `Persistable<UUID>`) — nenhuma feature do backlog atual precisa atualizar uma linha já
  existente, mas se isso mudar (ex.: zerar `mustChangePassword` após troca de senha), o
  mecanismo de `isNew()` precisa ser revisitado antes de reusar `save()` para isso. Ver
  `docs/services/auth-service.md`.
- Achado de processo real, corrigido mas pode se repetir em `bets-service`/`stats-service`
  (mesmo padrão de story criada com `jira_story.py` e commitada em `develop` antes da branch
  nascer): a PR final `feature/` → `develop` pode mostrar diff vazio em `CHANGELOG.md` mesmo
  com o arquivo correto, porque o `base.sha` que o GitHub Actions usa é o merge-base (fixo no
  ponto de divergência), não a ponta viva de `develop`. Ver `docs/CONVENTIONS.md` seção "Git"
  (nota nova) para o fluxo correto (não commitar a saída do `jira_story.py` em `develop` antes
  de criar a branch) e o procedimento de correção se acontecer de novo (`git merge develop`
  dentro da branch da feature + reconferir se o arquivo precisa ser reescrito depois do merge).
- **Achado crítico, corrigido em `feat-003.6`**: `MessageSourceAutoConfiguration` do Spring Boot
  nunca ativava neste serviço (exige `src/main/resources/messages.properties` **sem** sufixo de
  locale, confirmado via `javap` contra o jar real — só existiam `messages_pt_BR/en_US/es
  .properties`). Sem a autoconfiguração, `MessageSource` nunca virava bean real e todo
  `getMessage()` da aplicação lançava `NoSuchMessageException` via `DelegatingMessageSource`,
  silenciosamente, desde `feat-001.5` — nenhum teste pegou porque todos construíam
  `ResourceBundleMessageSource` manualmente em vez de usar o bean injetado. Corrigido criando o
  arquivo base. Ver `docs/CONVENTIONS.md` seção i18n — `bets-service`/`stats-service`/
  `api-gateway` precisam criar esse arquivo **junto** com seus próprios `messages_*.properties`,
  não depois de descobrir o bug de novo.
- **Risco residual aceito em `feat-003`**: corrida (TOCTOU) entre `gateway.exists(slug)` e
  `ensureSchemaExists(slug)` em `CreateTenantService` — dois `POST /api/v1/admin/tenants`
  concorrentes para o mesmo slug novo podem ambos passar no `exists()` antes de qualquer um
  provisionar; só a inserção do admin (não a criação do schema) tem fallback para
  `TenantAlreadyProvisionedException`. Aceito como desproporcional para uma rota admin de uso
  raro/manual (um operador só) — não implementar lock/compensação sem evidência real de
  concorrência.
- **Achado crítico, corrigido em `feat-006` (Persistence Auditor)**: `ApplicationRunner`/
  `CommandLineRunner` **não é seguro** para trabalho que precisa terminar antes do servidor
  aceitar tráfego — `SpringApplication.run()` já inicia o servidor embutido (`SmartLifecycle`,
  dentro de `refreshContext()`) **antes** de chamar os runners (`callRunners()`, depois do
  `refreshContext()` retornar). `PublicSchemaMigrationRunner` usava `ApplicationRunner` para
  migrar o schema `public` no boot — corrigido para `InitializingBean.afterPropertiesSet()`
  (roda durante `finishBeanFactoryInitialization()`, garantidamente antes do servidor subir,
  mesmo mecanismo do `FlywayMigrationInitializer` oficial do Spring Boot). Ver
  `docs/CONVENTIONS.md` seção "Migrations" — `bets-service`/`stats-service`/`api-gateway`
  reaproveitam o padrão se precisarem de algo equivalente a "rodar antes de servir tráfego".
- **Risco residual aceito em `feat-006`**: `JpaTelegramLinkRepository.upsert()` e o pré-check de
  `TelegramLinkConfirmationTransaction` são *find-then-save*, não atômicos — duas confirmações
  concorrentes para o mesmo `telegramUserId` podem gerar um `500` bruto em vez de um `409` limpo
  no perdedor da corrida (a transação inteira ainda reverte de forma consistente, sem corrupção
  de dado). Aceito como desproporcional para um endpoint interno de baixo volume, antes de
  `api-gateway` existir — mesma categoria do TOCTOU já aceito em `feat-003`. Sem job de limpeza
  para `PENDING_TELEGRAM_LINK` expirado e nunca confirmado — aceito, volume esperado é pequeno.

## Decisões tomadas

- Build tool: **Maven**. Arquitetura: **hexagonal**. Decididas em `../../docs/CONVENTIONS.md`.
- `feat-002.4`/`feat-002.5` (mapeamento JPA e multi-tenancy do Hibernate) foram absorvidas na
  mesma branch/PR — descobertas como interdependentes durante a implementação: um teste de
  integração de JPA não compila/passa sem o roteamento de schema já existir, e o gate de
  cobertura da CI roda em toda PR de subtask, não só no merge final.
- **Zero comentário de racional/documentação/regra de negócio em código** (endurece a regra de
  "no máximo uma linha" da sessão de `feat-001`) — todo racional vai para `docs/CONVENTIONS.md`/
  `docs/services/auth-service.md`, nunca para o código. Aplicado retroativamente também a
  `feat-001` nesta sessão (comentários e nomes de teste em português).
- Nomes de método de teste sempre em inglês, padrão `should...` — aplicado retroativamente a
  todos os testes de `feat-001`.
- `@Autowired` banido em todo o serviço, inclusive em teste (`spring.test.constructor.autowire.
  mode=all` via `src/test/resources/junit-platform.properties`) — injeção sempre por construtor.
- `package-info.java` removidos de todos os pacotes — duplicavam o diagrama de estrutura já
  documentado em `docs/CONVENTIONS.md`.

## Arquivos modificados nesta sessão

- `feat-002`: migration, `domain/model/{Role,User,TelegramAccount}`, `domain/port/out/*`,
  `adapter/out/persistence/*` (novo), `config/*` (novo — multi-tenancy), toda a suíte de testes
  reescrita — ver commits em `feature/SV-22` (mergeada em `develop`, `a847e48`).
- `feat-003`: `adapter/in/web/{AdminApiKeyFilter,AdminTenantController,CreateTenantRequest,
  CreateTenantResponse,DomainExceptionHandler,ProblemDetailMessages}` (novos),
  `adapter/out/security/{BCryptPasswordHasher,SecureRandomPasswordGenerator}` (novos),
  `application/CreateTenantService` (novo), `domain/model/{LocalizedDomainException,
  SlugRelatedDomainException,TenantAlreadyProvisionedException,InvalidTenantSlugException,
  InvalidAdminApiKeyException,CreatedTenantAdmin}` (novos), `domain/port/{in/CreateTenantUseCase,
  out/PasswordHasher,out/TemporaryPasswordGenerator}` (novos), `messages.properties` (novo, base
  sem locale), `pom.xml` (`spring-security-crypto`, `sourceEncoding`) — ver commits em
  `feature/SV-31` (mergeada em `develop`, `441355a`).
- `feat-006`: `domain/model/{TelegramLink,PendingTelegramLink,GeneratedTelegramLinkCode,
  CallerNotFoundException,TelegramLinkCodeNotFoundException,TelegramLinkCodeExpiredException,
  TelegramAccountAlreadyLinkedException,TelegramAccountNotFoundException}` (novos),
  `domain/port/{in/*TelegramLink*UseCase,in/LookupTelegramAccountUseCase,
  out/TelegramLinkRepository,out/PendingTelegramLinkRepository,out/TelegramLinkCodeGenerator}`
  (novos), `adapter/out/persistence/{TelegramLinkJpaEntity,PendingTelegramLinkJpaEntity,
  Jpa*Repository}` (novos, `@Table(schema = "public")`), `adapter/out/security/
  SecureRandomTelegramLinkCodeGenerator` (novo), `application/{GenerateTelegramLinkCodeService,
  ConfirmTelegramLinkService,TelegramLinkConfirmationTransaction,LookupTelegramAccountService}`
  (novos), `adapter/in/web/{TelegramLinksController,TelegramAccountsController,*Request,
  *Response}` (novos), `config/PublicSchemaMigrationRunner` (novo), `db/migration-public/`
  (novo) — ver commits em `feature/SV-50` (mergeada em `develop`, `a6f92fd`).

## Evidência de conclusão

- Ver campo `evidence` de `feat-002`/`feat-003`/`feat-004`/`feat-005`/`feat-006` em
  `feature_list.json` (objeto estruturado com 4 seções: verificação real executada, divergência
  do plano original, defeitos encontrados antes de causar dano, skills e ferramentas).

## Notas para a próxima sessão

- `feat-007` (pipeline de CI) é a única feature liberada restante deste serviço.
- Padrão novo de `feat-006`, reaproveitável por `bets-service`/`stats-service` se algum dia
  precisarem de uma tabela global fora do schema-per-tenant: entidade JPA com `@Table(schema =
  "public")` explícito (convive com a multi-tenancy do Hibernate sem precisar de um segundo
  mecanismo de acesso a dados) + migration eager separada via `InitializingBean` (nunca
  `ApplicationRunner`/`CommandLineRunner` — rodam depois do servidor já aceitar tráfego). Ver
  `docs/CONVENTIONS.md` seção "Migrations".
- `feat-005` emite o token mas não valida — a validação real (que injeta `X-User-Id`/
  `X-Tenant-Id` a partir do PASETO) é exclusiva de `services/api-gateway` (`epic-008`, ainda
  `not-started`). A mesma `PASETO_LOCAL_KEY` gerada aqui precisa ser configurada lá quando esse
  epic começar — não gerar uma independente (nota em `docs/OBSERVABILITY-AND-CONFIG.md`).
- Biblioteca `io.github.nbaars:paseto4j-version4:2024.3`: o README/main branch do projeto no
  GitHub está à frente do último release publicado — API real confirmada via `javap` contra o
  jar instalado, não confiar em exemplos do README sem checar a versão. Ver `PasetoAccessTokenIssuer`
  para a API real (`new SecretKey(byte[], Version)`, não `fromHexString`).
- O padrão de multi-tenancy do Hibernate (resolver + connection provider + `Persistable<UUID>`
  para entidade com id atribuído pelo domínio) já está documentado em `docs/CONVENTIONS.md` —
  `bets-service`/`stats-service` podem reaproveitar diretamente quando chegarem no próprio
  `feat-001`/mapeamento JPA, sem precisar rederivar as chaves de configuração do Hibernate via
  `javap` de novo (só reconferir se a versão instalada mudou).
