package com.stakevault.betting.auth.domain.port.in;

public interface ProvisionTenantSchemaUseCase {

	void ensureSchemaExists(String tenantSlug);

	void migrateIfPending(String tenantSlug);
}
