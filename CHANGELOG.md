# Changelog

Cada linha de `[Unreleased]` é um link para a issue do Jira que a gerou (story ou subtask),
formato `- [chave](url) - título` — sem prosa, sem categoria. Escrita automaticamente por
`tools/jira_story.py` no momento em que a issue é criada (ver `docs/CI-CD.md` seção "Changelog
por serviço"). O "porquê" de cada mudança vive na issue e na mensagem de commit, não aqui.

## [Unreleased]

- Remover todos os comentários restantes do código, testes e configuração (convenção de zero comentário, `docs/convencoes.md`)
- [SV-585](https://stakevault.atlassian.net/browse/SV-585) - Extrair LocalizedRuntimeException (2o harness do epic-034 da raiz)
- [SV-586](https://stakevault.atlassian.net/browse/SV-586) - Extrair LocalizedRuntimeException, migrar SlugRelatedDomainException e as 14 excecoes diretas
- [SV-587](https://stakevault.atlassian.net/browse/SV-587) - CHANGELOG e verificacao final

## [0.1.0] - 2026-09-23

- [SV-9](https://stakevault.atlassian.net/browse/SV-9) - Alinhar as chaves de projeto ao padrão do SonarCloud
- [SV-10](https://stakevault.atlassian.net/browse/SV-10) - Setup do projeto
- [SV-11](https://stakevault.atlassian.net/browse/SV-11) - Bootstrap do pom.xml e esqueleto hexagonal
- [SV-12](https://stakevault.atlassian.net/browse/SV-12) - Conexao Postgres com profiles dev/test/prod
- [SV-13](https://stakevault.atlassian.net/browse/SV-13) - Provisionamento de schema de tenant e migracao lazy
- [SV-14](https://stakevault.atlassian.net/browse/SV-14) - Gate de cobertura JaCoCo 80%
- [SV-15](https://stakevault.atlassian.net/browse/SV-15) - Scaffold de i18n (MessageSource) e teste smoke
- [SV-16](https://stakevault.atlassian.net/browse/SV-16) - Health checks do Actuator
- [SV-17](https://stakevault.atlassian.net/browse/SV-17) - .env.example e logging estruturado
- [SV-18](https://stakevault.atlassian.net/browse/SV-18) - CHANGELOG e verificacao final
- [SV-19](https://stakevault.atlassian.net/browse/SV-19) - Corrigir falso positivo do GitGuardian em .env.example
- [SV-20](https://stakevault.atlassian.net/browse/SV-20) - Resolver achados reais do SonarCloud vistos so na PR develop->main
- [SV-21](https://stakevault.atlassian.net/browse/SV-21) - Remover SQL de DDL escrito a mao; cortar comentarios longos
- [SV-22](https://stakevault.atlassian.net/browse/SV-22) - Entidades USER e TELEGRAM_ACCOUNT
- [SV-23](https://stakevault.atlassian.net/browse/SV-23) - Lombok e reorganizacao de TenantContextHolder
- [SV-24](https://stakevault.atlassian.net/browse/SV-24) - Migration Flyway: tabelas users e telegram_accounts
- [SV-25](https://stakevault.atlassian.net/browse/SV-25) - Modelo de dominio e ports/out
- [SV-26](https://stakevault.atlassian.net/browse/SV-26) - Entidades JPA e adapters de persistencia
- [SV-27](https://stakevault.atlassian.net/browse/SV-27) - Multi-tenancy do Hibernate por schema
- [SV-28](https://stakevault.atlassian.net/browse/SV-28) - Testes de mapeamento e isolamento por tenant
- [SV-29](https://stakevault.atlassian.net/browse/SV-29) - CHANGELOG e verificacao final
- [SV-30](https://stakevault.atlassian.net/browse/SV-30) - Corrigir 27 apontamentos do SonarCloud ignorados no merge e travar o gate
- [SV-31](https://stakevault.atlassian.net/browse/SV-31) - Provisionamento de tenant (rota admin)
- [SV-32](https://stakevault.atlassian.net/browse/SV-32) - Config ADMIN_API_KEY
- [SV-33](https://stakevault.atlassian.net/browse/SV-33) - Exceptions de dominio com chave de mensagem + entradas i18n
- [SV-34](https://stakevault.atlassian.net/browse/SV-34) - Ports/out + adapters de senha (hash e geracao)
- [SV-35](https://stakevault.atlassian.net/browse/SV-35) - CreateTenantUseCase / CreateTenantService
- [SV-36](https://stakevault.atlassian.net/browse/SV-36) - AdminApiKeyFilter
- [SV-37](https://stakevault.atlassian.net/browse/SV-37) - RestControllerAdvice + endpoint POST /api/v1/admin/tenants
- [SV-38](https://stakevault.atlassian.net/browse/SV-38) - Teste i18n (Accept-Language) + CHANGELOG + verificacao final
- [SV-39](https://stakevault.atlassian.net/browse/SV-39) - RF01 - Criacao de usuario dentro do tenant
- [SV-40](https://stakevault.atlassian.net/browse/SV-40) - Excecoes de dominio, i18n e handler de Bean Validation
- [SV-41](https://stakevault.atlassian.net/browse/SV-41) - CreateUserUseCase / CreateUserService
- [SV-42](https://stakevault.atlassian.net/browse/SV-42) - UsersController (POST /api/v1/users) + testes de integracao
- [SV-43](https://stakevault.atlassian.net/browse/SV-43) - CHANGELOG e verificacao final
- [SV-44](https://stakevault.atlassian.net/browse/SV-44) - RF02 - Autenticacao com token PASETO
- [SV-45](https://stakevault.atlassian.net/browse/SV-45) - Dependencia PASETO + config da chave simetrica
- [SV-46](https://stakevault.atlassian.net/browse/SV-46) - AccessTokenIssuer (emissao de token PASETO)
- [SV-47](https://stakevault.atlassian.net/browse/SV-47) - PasswordHasher.matches() + InvalidCredentialsException + LoginUseCase/LoginService
- [SV-48](https://stakevault.atlassian.net/browse/SV-48) - AuthController (POST /api/v1/auth/login) + testes de integracao + docs
- [SV-49](https://stakevault.atlassian.net/browse/SV-49) - CHANGELOG e verificacao final
- [SV-50](https://stakevault.atlassian.net/browse/SV-50) - RF05 (suporte) - Vinculo de conta Telegram
- [SV-51](https://stakevault.atlassian.net/browse/SV-51) - Migration eager do schema public + runner no boot
- [SV-52](https://stakevault.atlassian.net/browse/SV-52) - Dominio: TelegramLink, PendingTelegramLink, excecoes, TenantSchemaName.slug(), gerador de codigo
- [SV-53](https://stakevault.atlassian.net/browse/SV-53) - Persistencia JPA das tabelas public (TelegramLink, PendingTelegramLink)
- [SV-54](https://stakevault.atlassian.net/browse/SV-54) - Application services: gerar codigo, confirmar vinculo, lookup
- [SV-55](https://stakevault.atlassian.net/browse/SV-55) - Web adapter: 3 endpoints + DTOs + i18n
- [SV-56](https://stakevault.atlassian.net/browse/SV-56) - CHANGELOG e verificacao final
- [SV-57](https://stakevault.atlassian.net/browse/SV-57) - Pipeline de CI (GitHub Actions + SonarCloud)
- [SV-58](https://stakevault.atlassian.net/browse/SV-58) - Auditoria do pipeline real vs description da feature
- [SV-59](https://stakevault.atlassian.net/browse/SV-59) - CHANGELOG e verificacao final
- [SV-159](https://stakevault.atlassian.net/browse/SV-159) - Porta HTTP fixa (8081)
- [SV-160](https://stakevault.atlassian.net/browse/SV-160) - server.port fixo, CHANGELOG e verificacao final
- [SV-222](https://stakevault.atlassian.net/browse/SV-222) - GET /api/v1/users - listagem de usuarios do tenant
- [SV-223](https://stakevault.atlassian.net/browse/SV-223) - ListUsersUseCase / ListUsersService + UserRepository.findAllOrderByName
- [SV-224](https://stakevault.atlassian.net/browse/SV-224) - UsersController (GET /api/v1/users) + UserSummaryResponse + testes de integracao
- [SV-225](https://stakevault.atlassian.net/browse/SV-225) - CHANGELOG e verificacao final
- [SV-226](https://stakevault.atlassian.net/browse/SV-226) - POST /api/v1/auth/login retorna userId e role
- [SV-227](https://stakevault.atlassian.net/browse/SV-227) - LoginResult/LoginResponse ganham userId+role, CHANGELOG e verificacao final
- [SV-276](https://stakevault.atlassian.net/browse/SV-276) - Dockerfile para imagem de producao
- [SV-277](https://stakevault.atlassian.net/browse/SV-277) - Dockerfile multi-stage + verificacao real do container contra a infra
- [SV-311](https://stakevault.atlassian.net/browse/SV-311) - Claim role no token PASETO (X-User-Role)
- [SV-312](https://stakevault.atlassian.net/browse/SV-312) - PasetoClaims ganha role, issue() recebe Role
- [SV-315](https://stakevault.atlassian.net/browse/SV-315) - Corrigir casing do claim role (lowercase, nao role.name() cru)
- [SV-316](https://stakevault.atlassian.net/browse/SV-316) - role.name().toLowerCase() no claim do PASETO
- [SV-325](https://stakevault.atlassian.net/browse/SV-325) - CI: build e push da imagem Docker pro GHCR
- [SV-326](https://stakevault.atlassian.net/browse/SV-326) - Job build-and-push-image no ci.yml
- [SV-327](https://stakevault.atlassian.net/browse/SV-327) - CHANGELOG e verificacao final
- [SV-335](https://stakevault.atlassian.net/browse/SV-335) - Corrigir SHA pin (S7637) e guard de qualitygate.wait achados na verificacao
- [SV-338](https://stakevault.atlassian.net/browse/SV-338) - mvnw sem bit de execucao quebrava o build real da imagem Docker
- [SV-382](https://stakevault.atlassian.net/browse/SV-382) - Orquestrar provisionamento de tenant em bets-service/stats-service (reverte 3 chamadas manuais)
- [SV-383](https://stakevault.atlassian.net/browse/SV-383) - RestClientDownstreamTenantProvisioner + orquestracao no CreateTenantService
- [SV-384](https://stakevault.atlassian.net/browse/SV-384) - CHANGELOG e verificacao final
- [SV-432](https://stakevault.atlassian.net/browse/SV-432) - CD: job de deploy automatico (kubectl rollout restart) no ci.yml
- [SV-433](https://stakevault.atlassian.net/browse/SV-433) - Job deploy no ci.yml
- [SV-434](https://stakevault.atlassian.net/browse/SV-434) - CHANGELOG e verificacao final
- [SV-486](https://stakevault.atlassian.net/browse/SV-486) - PATCH /api/v1/users/{id} - atualizar usuario do tenant
- [SV-487](https://stakevault.atlassian.net/browse/SV-487) - Domain + application + persistencia (UpdateUserUseCase, guarda de ultimo admin, JpaUserRepository.update)
- [SV-488](https://stakevault.atlassian.net/browse/SV-488) - UsersController (PATCH /api/v1/users/{id}) + DTOs + testes de integracao + docs
- [SV-489](https://stakevault.atlassian.net/browse/SV-489) - CHANGELOG e verificacao final
- [SV-510](https://stakevault.atlassian.net/browse/SV-510) - Endpoint de troca de senha (usuario logado)
- [SV-511](https://stakevault.atlassian.net/browse/SV-511) - Domain + application + i18n (ChangePasswordUseCase, ChangePasswordService, CurrentPasswordMismatchException)
- [SV-512](https://stakevault.atlassian.net/browse/SV-512) - AuthController (POST /api/v1/auth/change-password) + DTO + testes de integracao + docs
- [SV-513](https://stakevault.atlassian.net/browse/SV-513) - CHANGELOG e verificacao final
- [SV-549](https://stakevault.atlassian.net/browse/SV-549) - Reformulacao de marca: StakeVault -> Arka (pom.xml description)
- [SV-550](https://stakevault.atlassian.net/browse/SV-550) - pom.xml: description 'StakeVault' -> 'Arka'
- [SV-551](https://stakevault.atlassian.net/browse/SV-551) - CHANGELOG, verificacao final e revisao do vault
- [SV-561](https://stakevault.atlassian.net/browse/SV-561) - CI: gerar versao (semver + tag + Release + bump de pom.xml + corte de CHANGELOG) ao merge em main
- [SV-562](https://stakevault.atlassian.net/browse/SV-562) - Job 'release' no ci.yml + .github/scripts/cut-changelog.py
- [SV-563](https://stakevault.atlassian.net/browse/SV-563) - CHANGELOG, verificacao final e revisao do vault (documentar o mecanismo em docs/pipeline-ci-cd.md, raiz)
