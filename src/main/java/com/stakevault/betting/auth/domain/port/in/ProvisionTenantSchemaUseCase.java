package com.stakevault.betting.auth.domain.port.in;

/**
 * Provisionamento e migracao lazy de schema de tenant - ver docs/CONVENTIONS.md secao
 * "Migrations" e docs/TESTING.md secao "Schema-per-tenant nos testes de integracao".
 */
public interface ProvisionTenantSchemaUseCase {

	/**
	 * Cria o schema se ainda nao existir e roda o Flyway completo. Uso: rota admin de
	 * criacao de tenant (feat-003) e fixtures de teste - nunca chamado a partir do
	 * caminho por requisicao.
	 */
	void ensureSchemaExists(String tenantSlug);

	/**
	 * Roda migrations pendentes de um schema que ja precisa existir. Lanca
	 * {@link com.stakevault.betting.auth.domain.model.TenantSchemaNotFoundException} se
	 * o schema nao existir - nunca cria. Uso: filtro por requisicao (X-Tenant-Id).
	 */
	void migrateIfPending(String tenantSlug);
}
