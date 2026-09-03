package com.stakevault.betting.auth.domain.model;

/** Schema de tenant referenciado por X-Tenant-Id que nunca foi provisionado (feat-003). */
public class TenantSchemaNotFoundException extends RuntimeException {

	public TenantSchemaNotFoundException(TenantSchemaName schema) {
		super("schema de tenant nao provisionado: " + schema.value());
	}
}
