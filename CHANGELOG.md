# Changelog

Cada linha de `[Unreleased]` é um link para a issue do Jira que a gerou (story ou subtask),
formato `- [chave](url) - título` — sem prosa, sem categoria. Escrita automaticamente por
`tools/jira_story.py` no momento em que a issue é criada (ver `docs/CI-CD.md` seção "Changelog
por serviço"). O "porquê" de cada mudança vive na issue e na mensagem de commit, não aqui.

## [Unreleased]

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
