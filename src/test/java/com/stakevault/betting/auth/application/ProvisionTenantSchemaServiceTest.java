package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.TenantSchemaNotFoundException;
import com.stakevault.betting.auth.domain.port.out.TenantSchemaGateway;

@ExtendWith(MockitoExtension.class)
class ProvisionTenantSchemaServiceTest {

	@Mock
	private TenantSchemaGateway gateway;

	private ProvisionTenantSchemaService service;

	@BeforeEach
	void setUp() {
		service = new ProvisionTenantSchemaService(gateway);
	}

	@Test
	void ensureSchemaExists_criaQuandoSchemaNaoExiste() {
		when(gateway.exists(new TenantSchemaName("tenant_acme"))).thenReturn(false);

		service.ensureSchemaExists("acme");

		verify(gateway).create(new TenantSchemaName("tenant_acme"));
		verify(gateway).migrate(new TenantSchemaName("tenant_acme"));
	}

	@Test
	void ensureSchemaExists_naoCriaDeNovoQuandoSchemaJaExiste() {
		when(gateway.exists(new TenantSchemaName("tenant_acme"))).thenReturn(true);

		service.ensureSchemaExists("acme");

		verify(gateway, never()).create(any());
		verify(gateway, times(1)).migrate(new TenantSchemaName("tenant_acme"));
	}

	@Test
	void migrateIfPending_migraQuandoSchemaExiste() {
		when(gateway.exists(new TenantSchemaName("tenant_acme"))).thenReturn(true);

		service.migrateIfPending("acme");

		verify(gateway).migrate(new TenantSchemaName("tenant_acme"));
		verify(gateway, never()).create(any());
	}

	@Test
	void migrateIfPending_lancaExceptionSemMigrarQuandoSchemaNaoExiste() {
		when(gateway.exists(new TenantSchemaName("tenant_acme"))).thenReturn(false);

		assertThatThrownBy(() -> service.migrateIfPending("acme"))
				.isInstanceOf(TenantSchemaNotFoundException.class);

		verify(gateway, never()).migrate(any());
		verify(gateway, never()).create(any());
	}
}
