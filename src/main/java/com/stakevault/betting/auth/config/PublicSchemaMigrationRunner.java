package com.stakevault.betting.auth.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PublicSchemaMigrationRunner implements ApplicationRunner {

	private static final String MIGRATION_LOCATION = "classpath:db/migration-public";
	private static final String SCHEMA = "public";

	private final DataSource dataSource;

	public PublicSchemaMigrationRunner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) {
		Flyway.configure()
				.dataSource(dataSource)
				.schemas(SCHEMA)
				.createSchemas(false)
				.locations(MIGRATION_LOCATION)
				.load()
				.migrate();
	}
}
