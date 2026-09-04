package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
	void shouldDelegateExistenceCheckToGateway() {
		when(gateway.exists(new TenantSchemaName("tenant_acme"))).thenReturn(true);

		assertThat(service.exists("acme")).isTrue();
	}

	@Test
	void shouldDelegateCreationAndMigrationToGateway() {
		service.ensureSchemaExists("acme");

		verify(gateway).createAndMigrate(new TenantSchemaName("tenant_acme"));
	}

	@Test
	void shouldMigrateWhenSchemaExists() {
		when(gateway.exists(new TenantSchemaName("tenant_acme"))).thenReturn(true);

		service.migrateIfPending("acme");

		verify(gateway).migrateExistingOnly(new TenantSchemaName("tenant_acme"));
	}

	@Test
	void shouldThrowWithoutMigratingWhenSchemaDoesNotExist() {
		when(gateway.exists(new TenantSchemaName("tenant_acme"))).thenReturn(false);

		assertThatThrownBy(() -> service.migrateIfPending("acme"))
				.isInstanceOf(TenantSchemaNotFoundException.class);

		verify(gateway, never()).migrateExistingOnly(new TenantSchemaName("tenant_acme"));
	}

	@Test
	void shouldCheckExistenceOnEveryCallEvenWhenRepeated() {
		when(gateway.exists(new TenantSchemaName("tenant_acme"))).thenReturn(true, false);
		service.migrateIfPending("acme");

		assertThatThrownBy(() -> service.migrateIfPending("acme"))
				.isInstanceOf(TenantSchemaNotFoundException.class);

		verify(gateway, times(2)).exists(new TenantSchemaName("tenant_acme"));
		verify(gateway, times(1)).migrateExistingOnly(new TenantSchemaName("tenant_acme"));
	}
}
