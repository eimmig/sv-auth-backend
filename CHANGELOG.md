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
