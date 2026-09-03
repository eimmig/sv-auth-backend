package com.stakevault.betting.auth.domain.port.out;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;

/** Acesso ao schema Postgres de um tenant - implementado em adapter/out/persistence. */
public interface TenantSchemaGateway {

	boolean exists(TenantSchemaName schema);

	void create(TenantSchemaName schema);

	/** Roda as migrations do Flyway pendentes, escopadas a este schema. Idempotente. */
	void migrate(TenantSchemaName schema);
}
