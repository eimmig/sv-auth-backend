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
		// gateway.exists() roda sempre, sem cache: e a checagem de seguranca que
		// confirma que o tenant ainda esta provisionado a cada requisicao (barato -
		// uma linha indexada de information_schema.schemata). So gateway.migrate()
		// e cacheado (JdbcFlywayTenantSchemaGateway) - isso e custo (scan de
		// classpath + lock do Flyway), nao seguranca, entao pode ser pulado com
		// seguranca quando ja se sabe que o schema esta em dia.
		//
		// Uma versao anterior desta classe tambem cacheava o resultado do exists()
		// aqui - /code-review pegou antes do merge: um schema derrubado em runtime
		// (DROP SCHEMA manual, por exemplo) continuaria autorizando requisicoes ate
		// o processo reiniciar, silenciosamente. Revertido.
		TenantSchemaName schema = TenantSchemaName.fromSlug(tenantSlug);
		if (!gateway.exists(schema)) {
			throw new TenantSchemaNotFoundException(schema);
		}
		gateway.migrate(schema);
	}
}
