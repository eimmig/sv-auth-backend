package com.stakevault.betting.auth.application;

import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.TenantSchemaNotFoundException;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.TenantSchemaGateway;

@Service
public class ProvisionTenantSchemaService implements ProvisionTenantSchemaUseCase {

	private final TenantSchemaGateway gateway;

	public ProvisionTenantSchemaService(TenantSchemaGateway gateway) {
		this.gateway = gateway;
	}

	@Override
	public void ensureSchemaExists(String tenantSlug) {
		TenantSchemaName schema = TenantSchemaName.fromSlug(tenantSlug);
		if (!gateway.exists(schema)) {
			gateway.create(schema);
		}
		gateway.migrate(schema);
	}

	@Override
	public void migrateIfPending(String tenantSlug) {
		TenantSchemaName schema = TenantSchemaName.fromSlug(tenantSlug);
		if (!gateway.exists(schema)) {
			throw new TenantSchemaNotFoundException(schema);
		}
		gateway.migrate(schema);
	}
}
