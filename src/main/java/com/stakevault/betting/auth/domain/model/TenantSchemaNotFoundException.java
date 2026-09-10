package com.stakevault.betting.auth.domain.model;

public class TenantSchemaNotFoundException extends RuntimeException {

	public TenantSchemaNotFoundException(TenantSchemaName schema) {
		super("schema de tenant nao provisionado: " + schema.value());
	}
}
