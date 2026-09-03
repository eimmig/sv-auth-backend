package com.stakevault.betting.auth.domain.port.out;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;

public interface TenantSchemaGateway {

	boolean exists(TenantSchemaName schema);

	void createAndMigrate(TenantSchemaName schema);

	void migrateExistingOnly(TenantSchemaName schema);
}
