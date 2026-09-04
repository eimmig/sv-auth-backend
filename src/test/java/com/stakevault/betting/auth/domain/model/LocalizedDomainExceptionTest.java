package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LocalizedDomainExceptionTest {

	@Test
	void shouldBeCatchableByInterfaceType() {
		RuntimeException[] exceptions = {
				new TenantAlreadyProvisionedException("acme"),
				new InvalidTenantSlugException("1acme", new IllegalArgumentException()),
				new InvalidAdminApiKeyException(),
				new MissingCallerContextException(),
				new MissingTenantContextException(),
				new AdminRoleRequiredException(),
				new EmailAlreadyRegisteredException("member@acme")
		};

		for (RuntimeException exception : exceptions) {
			assertThat(exception).isInstanceOf(LocalizedDomainException.class);
			assertThat(((LocalizedDomainException) exception).messageKey()).isNotBlank();
		}
	}
}
