package com.stakevault.betting.auth.support;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.stakevault.betting.auth.TestcontainersConfiguration;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class TenantSchemaIntegrationSupport {

	protected static final ChronoUnit DB_PRECISION = ChronoUnit.MICROS;

	protected final ProvisionTenantSchemaUseCase provisionTenantSchema;
	protected final JdbcTemplate jdbcTemplate;

	protected String tenantSlug;
	protected TenantSchemaName schema;

	protected TenantSchemaIntegrationSupport(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		this.provisionTenantSchema = provisionTenantSchema;
		this.jdbcTemplate = jdbcTemplate;
	}

	@BeforeEach
	void provisionTestTenant() {
		tenantSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		schema = TenantSchemaName.fromSlug(tenantSlug);
		provisionTenantSchema.ensureSchemaExists(tenantSlug);
	}

	@AfterEach
	void dropTestTenant() {
		jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + schema.value() + "\" CASCADE");
	}
}
