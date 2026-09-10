package com.stakevault.betting.auth.domain.port.in;

public interface ProvisionTenantSchemaUseCase {

	boolean exists(String tenantSlug);

	void ensureSchemaExists(String tenantSlug);

	void migrateIfPending(String tenantSlug);
}
