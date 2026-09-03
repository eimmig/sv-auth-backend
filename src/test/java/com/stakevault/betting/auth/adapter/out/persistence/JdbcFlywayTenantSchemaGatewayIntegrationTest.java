package com.stakevault.betting.auth.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.TenantSchemaNotFoundException;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

class JdbcFlywayTenantSchemaGatewayIntegrationTest extends TenantSchemaIntegrationSupport {

	@Test
	void ensureSchemaExists_criaSchemaEMigraSemFalhar() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, schema.value());

		assertThat(count).isEqualTo(1);
	}

	@Test
	void ensureSchemaExists_eIdempotente() {
		provisionTenantSchema.ensureSchemaExists(tenantSlug);

		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, schema.value());
		assertThat(count).isEqualTo(1);
	}

	@Test
	void migrateIfPending_rodaSemFalharQuandoSchemaJaExiste() {
		assertThatCode(() -> provisionTenantSchema.migrateIfPending(tenantSlug)).doesNotThrowAnyException();
	}

	@Test
	void migrateIfPending_naSegundaChamadaUsaOCache() {
		assertThatCode(() -> {
			provisionTenantSchema.migrateIfPending(tenantSlug);
			provisionTenantSchema.migrateIfPending(tenantSlug);
		}).doesNotThrowAnyException();
	}

	@Test
	void migrateExistingOnly_comCacheFrioMigraSchemaJaExistente() {
		JdbcFlywayTenantSchemaGateway freshGateway = new JdbcFlywayTenantSchemaGateway(jdbcTemplate.getDataSource());

		assertThatCode(() -> freshGateway.migrateExistingOnly(schema)).doesNotThrowAnyException();
	}

	@Test
	void migrateIfPending_lancaExceptionSemCriarSchemaQuandoTenantNaoProvisionado() {
		String slugInexistente = "test-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		TenantSchemaName schemaInexistente = TenantSchemaName.fromSlug(slugInexistente);

		assertThatThrownBy(() -> provisionTenantSchema.migrateIfPending(slugInexistente))
				.isInstanceOf(TenantSchemaNotFoundException.class);

		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, schemaInexistente.value());
		assertThat(count).isZero();
	}
}
