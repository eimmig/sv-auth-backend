package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TenantAlreadyProvisionedExceptionTest {

	@Test
	void shouldExposeMessageKeyAndSlug() {
		var exception = new TenantAlreadyProvisionedException("acme");

		assertThat(exception.messageKey()).isEqualTo("error.tenant-already-provisioned");
		assertThat(exception.slug()).isEqualTo("acme");
	}
}
