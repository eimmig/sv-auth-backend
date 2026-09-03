package com.stakevault.betting.auth.adapter.out.persistence;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.port.out.TenantSchemaGateway;

@Component
public class JdbcFlywayTenantSchemaGateway implements TenantSchemaGateway {

	private static final String MIGRATION_LOCATION = "classpath:db/migration";

	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;

	// TenantSchemaFilter chama migrate() a cada requisicao com X-Tenant-Id (migracao
	// lazy). Sem isso, cada requisicao pagaria scan de classpath + lock advisory do
	// Flyway de novo, mesmo sem nada pendente. Escopo de JVM: reinicio (= deploy) e
	// o unico jeito de uma migration nova ser pega por um schema ja cacheado aqui -
	// aceitavel porque deploy ja reinicia o processo.
	private final Set<String> migratedSchemas = ConcurrentHashMap.newKeySet();

	public JdbcFlywayTenantSchemaGateway(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public boolean exists(TenantSchemaName schema) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
				Integer.class, schema.value());
		return count != null && count > 0;
	}

	@Override
	public void create(TenantSchemaName schema) {
		// Identificador nao pode ser bind parameter em DDL - seguro porque TenantSchemaName
		// ja validou o charset (regex) no construtor; aspas duplas cobrem o hifen, invalido
		// num identificador Postgres sem aspas.
		jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"" + schema.value() + "\"");
	}

	@Override
	public void migrate(TenantSchemaName schema) {
		if (migratedSchemas.contains(schema.value())) {
			return;
		}
		Flyway.configure()
				.dataSource(dataSource)
				.schemas(schema.value())
				.locations(MIGRATION_LOCATION)
				.load()
				.migrate();
		// So marca depois do migrate() nao lancar - uma migration com erro nunca fica
		// presa como "ja migrada", e a proxima requisicao tenta de novo.
		migratedSchemas.add(schema.value());
	}
}
