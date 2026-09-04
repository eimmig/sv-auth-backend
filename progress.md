# Log de Progresso — auth-service

## Estado Atual (Current State)

**Última atualização:** 2026-09-04
**Feature ativa:** nenhuma (`feat-004` `done`, `feat-005`/`feat-007` liberadas — `feat-006`
depende de `feat-005`)

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

### Em andamento

- Nenhuma feature iniciada.

### Próximos passos (Next Steps)

1. `feat-005` (RF02, login/PASETO) e `feat-007` (pipeline de CI) estão liberadas — `feat-006`
   (vínculo Telegram) depende de `feat-005`.

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

## Evidência de conclusão

- Ver campo `evidence` de `feat-002`/`feat-003`/`feat-004` em `feature_list.json` (objeto
  estruturado com 4 seções: verificação real executada, divergência do plano original, defeitos
  encontrados antes de causar dano, skills e ferramentas).

## Notas para a próxima sessão

- `feat-005` (RF02, login/PASETO) é a próxima liberada. `feat-004` resolveu a checagem de "quem
  está autenticado" via `X-User-Id`/`X-Tenant-Id` confiados diretamente (sem `api-gateway`
  ainda) — `feat-005` precisa decidir se mantém esse modelo até `epic-008` existir ou se
  antecipa algo. Reaproveita `PasswordHasher`/`UserRepository`/`AttributeConverter` já
  existentes.
- `feat-007` (pipeline de CI) também está liberada, independente de `feat-005`.
- O padrão de multi-tenancy do Hibernate (resolver + connection provider + `Persistable<UUID>`
  para entidade com id atribuído pelo domínio) já está documentado em `docs/CONVENTIONS.md` —
  `bets-service`/`stats-service` podem reaproveitar diretamente quando chegarem no próprio
  `feat-001`/mapeamento JPA, sem precisar rederivar as chaves de configuração do Hibernate via
  `javap` de novo (só reconferir se a versão instalada mudou).
