package com.stakevault.betting.auth.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.TenantSchemaNotFoundException;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

class JdbcFlywayTenantSchemaGatewayIntegrationTest extends TenantSchemaIntegrationSupport {

	JdbcFlywayTenantSchemaGatewayIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	@Test
	void shouldCreateAndMigrateSchema() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, schema.value());

		assertThat(count).isEqualTo(1);
	}

	@Test
	void shouldBeIdempotentWhenCalledTwice() {
		provisionTenantSchema.ensureSchemaExists(tenantSlug);

		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, schema.value());
		assertThat(count).isEqualTo(1);
	}

	@Test
	void shouldNotFailWhenSchemaAlreadyExists() {
		assertThatCode(() -> provisionTenantSchema.migrateIfPending(tenantSlug)).doesNotThrowAnyException();
	}

	@Test
	void shouldUseCacheOnSecondCall() {
		assertThatCode(() -> {
			provisionTenantSchema.migrateIfPending(tenantSlug);
			provisionTenantSchema.migrateIfPending(tenantSlug);
		}).doesNotThrowAnyException();
	}

	@Test
	void shouldMigrateExistingSchemaWithColdCache() {
		JdbcFlywayTenantSchemaGateway freshGateway = new JdbcFlywayTenantSchemaGateway(jdbcTemplate.getDataSource());

		assertThatCode(() -> freshGateway.migrateExistingOnly(schema)).doesNotThrowAnyException();
	}

	@Test
	void shouldThrowWithoutCreatingSchemaWhenTenantNotProvisioned() {
		String nonExistentSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		TenantSchemaName nonExistentSchema = TenantSchemaName.fromSlug(nonExistentSlug);

		assertThatThrownBy(() -> provisionTenantSchema.migrateIfPending(nonExistentSlug))
				.isInstanceOf(TenantSchemaNotFoundException.class);

		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, nonExistentSchema.value());
		assertThat(count).isZero();
	}
}
