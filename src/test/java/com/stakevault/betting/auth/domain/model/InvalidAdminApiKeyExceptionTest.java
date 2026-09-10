package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InvalidAdminApiKeyExceptionTest {

	@Test
	void shouldExposeMessageKey() {
		assertThat(new InvalidAdminApiKeyException().messageKey()).isEqualTo("error.invalid-admin-api-key");
	}

	@Test
	void shouldMapToUnauthorizedStatusWithNoMessageArgs() {
		var exception = new InvalidAdminApiKeyException();

		assertThat(exception.httpStatusCode()).isEqualTo(401);
		assertThat(exception.messageArgs()).isEmpty();
	}
}
