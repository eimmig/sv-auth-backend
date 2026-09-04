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

	@Test
	void shouldMapToUnprocessableEntityStatusWithSlugAsMessageArg() {
		var exception = new InvalidTenantSlugException("1acme", new IllegalArgumentException());

		assertThat(exception.httpStatusCode()).isEqualTo(422);
		assertThat(exception.messageArgs()).containsExactly("1acme");
	}

	@Test
	void shouldReplaceNullSlugWithEmptyStringInMessageArgs() {
		var exception = new InvalidTenantSlugException(null, new IllegalArgumentException());

		assertThat(exception.slug()).isNull();
		assertThat(exception.messageArgs()).containsExactly("");
	}
}
