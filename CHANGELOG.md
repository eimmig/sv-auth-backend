# Changelog

Todas as mudanças notáveis deste serviço são documentadas neste arquivo. Formato baseado em
[Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/). Toda feature que altera este
serviço adiciona uma entrada em `[Unreleased]` — verificado automaticamente pela pipeline de CI
(ver `docs/CI-CD.md`).

## [Unreleased]

### Added

- Esqueleto do projeto Spring Boot 4.1.1 (Java 25, Maven), gerado via Spring Initializr e
  reorganizado no layout hexagonal (`domain/{model,port/in,port/out}`, `application/`,
  `adapter/{in/web,out/persistence}`, `config/`) definido em `docs/CONVENTIONS.md`.

### Fixed

- Passo de validação de i18n da CI ganhou marcador próprio (`messages_pt_BR.properties`), em vez
  de depender do `pom.xml` que guarda os demais passos: como `feat-001` foi dividida em subtasks
  incrementais, `pom.xml` nasce bem antes dos arquivos de tradução, e o guard antigo quebraria os
  PRs intermediários por sequenciamento, não por defeito real (ver `docs/CI-CD.md`).
- Chave do projeto no SonarCloud corrigida para `eimmig_sv-auth-backend`. O SonarCloud gera a chave como
  `<org>_<repo>` ao importar um repositório do GitHub; a forma sem prefixo, usada até aqui, faria a
  análise falhar com projeto inexistente.
