package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InvalidAdminApiKeyExceptionTest {

	@Test
	void shouldExposeMessageKey() {
		assertThat(new InvalidAdminApiKeyException().messageKey()).isEqualTo("error.invalid-admin-api-key");
	}
}
