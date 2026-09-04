package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InvalidTenantSlugExceptionTest {

	@Test
	void shouldExposeMessageKeySlugAndCause() {
		var cause = new IllegalArgumentException("bad slug");
		var exception = new InvalidTenantSlugException("1acme", cause);

		assertThat(exception.messageKey()).isEqualTo("error.invalid-tenant-slug");
		assertThat(exception.slug()).isEqualTo("1acme");
		assertThat(exception.getCause()).isSameAs(cause);
	}
}
