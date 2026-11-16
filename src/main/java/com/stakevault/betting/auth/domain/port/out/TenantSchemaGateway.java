package com.stakevault.betting.auth.domain.port.out;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;

public interface TenantSchemaGateway {

	boolean exists(TenantSchemaName schema);

	void createAndMigrate(TenantSchemaName schema);

	void migrateExistingOnly(TenantSchemaName schema);

	/**
	 * Apaga o schema da organizacao (usuarios e contas do Telegram) e os vinculos e codigos pendentes do
	 * Telegram dela no schema public (epic-065). Nao falha quando o schema nao existe.
	 */
	void drop(TenantSchemaName schema);
}
