# Log de Progresso — auth-service

## Estado Atual (Current State)

**Última atualização:** 2026-09-04
**Feature ativa:** nenhuma (`feat-002` `done`, `feat-003` liberada)

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
      7 subtasks (SV-23..SV-29). Evidência completa em `feature_list.json`.

### Em andamento

- Nenhuma feature iniciada.

### Próximos passos (Next Steps)

1. `feat-003` — rota administrativa de provisionamento de tenant (`X-Admin-Api-Key`, cria o
   primeiro `admin` com `mustChangePassword = true`). Primeira feature a expor endpoint HTTP
   real e a primeira que vai precisar decidir hashing de senha (BCrypt ou similar, ainda não
   escolhido em nenhuma convenção).

## Bloqueios / Riscos

- Nenhum bloqueio real.
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

## Evidência de conclusão

- Ver campo `evidence` de `feat-002` em `feature_list.json` (objeto estruturado com 4 seções:
  verificação real executada, divergência do plano original, defeitos encontrados antes de
  causar dano, skills e ferramentas).

## Notas para a próxima sessão

- `feat-003` é a primeira feature deste serviço com endpoint HTTP real — vai precisar decidir
  biblioteca de hashing de senha (não escolhida em nenhuma convenção ainda).
- O padrão de multi-tenancy do Hibernate (resolver + connection provider + `Persistable<UUID>`
  para entidade com id atribuído pelo domínio) já está documentado em `docs/CONVENTIONS.md` —
  `bets-service`/`stats-service` podem reaproveitar diretamente quando chegarem no próprio
  `feat-001`/mapeamento JPA, sem precisar rederivar as chaves de configuração do Hibernate via
  `javap` de novo (só reconferir se a versão instalada mudou).
