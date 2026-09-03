package com.stakevault.betting.auth.support;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.stakevault.betting.auth.TestcontainersConfiguration;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;

/**
 * Provisiona um schema de tenant de teste (slug unico por classe) chamando o mesmo caso
 * de uso de producao - ver docs/TESTING.md secao "Schema-per-tenant nos testes de
 * integracao". Schema derrubado ao final de cada teste.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class TenantSchemaIntegrationSupport {

	@Autowired
	protected ProvisionTenantSchemaUseCase provisionTenantSchema;

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	protected String tenantSlug;
	protected TenantSchemaName schema;

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
