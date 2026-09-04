package com.stakevault.betting.auth.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

/** Connection.setSchema() usa SET SEARCH_PATH internamente no driver do Postgres. */
@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

	private final DataSource dataSource;

	public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		Connection connection = getAnyConnection();
		connection.setSchema(tenantIdentifier);
		return connection;
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		connection.setSchema("public");
		releaseAnyConnection(connection);
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	@Override
	public boolean isUnwrappableAs(Class<?> unwrapType) {
		return unwrapType.isInstance(this);
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		return unwrapType.isInstance(this) ? unwrapType.cast(this) : null;
	}
}
