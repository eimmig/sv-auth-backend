package com.stakevault.betting.auth.domain.port.in;

public interface ProvisionTenantSchemaUseCase {

	/** Cria o schema se nao existir e migra. Uso: rota admin (feat-003) e fixtures de teste. */
	void ensureSchemaExists(String tenantSlug);

	/** Migra um schema ja existente; lanca TenantSchemaNotFoundException se nao existir. */
	void migrateIfPending(String tenantSlug);
}
