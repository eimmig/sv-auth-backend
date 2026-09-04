package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LocalizedDomainExceptionTest {

	@Test
	void shouldBeCatchableByInterfaceType() {
		RuntimeException[] exceptions = {
				new TenantAlreadyProvisionedException("acme"),
				new InvalidTenantSlugException("1acme", new IllegalArgumentException()),
				new InvalidAdminApiKeyException()
		};

		for (RuntimeException exception : exceptions) {
			assertThat(exception).isInstanceOf(LocalizedDomainException.class);
			assertThat(((LocalizedDomainException) exception).messageKey()).isNotBlank();
		}
	}
}
