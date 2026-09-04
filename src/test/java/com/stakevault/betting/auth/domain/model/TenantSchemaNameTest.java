package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TenantSchemaNameTest {

	@Test
	void shouldDeriveTenantPrefixFromSlug() {
		assertThat(TenantSchemaName.fromSlug("acme").value()).isEqualTo("tenant_acme");
	}

	@Test
	void shouldAcceptHyphenInTheMiddle() {
		assertThat(TenantSchemaName.fromSlug("acme-corp").value()).isEqualTo("tenant_acme-corp");
	}

	@Test
	void shouldRejectSlugStartingWithDigit() {
		assertThatThrownBy(() -> TenantSchemaName.fromSlug("1acme")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectUppercaseSlug() {
		assertThatThrownBy(() -> TenantSchemaName.fromSlug("ACME")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectInvalidCharacter() {
		assertThatThrownBy(() -> TenantSchemaName.fromSlug("acme!")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectSingleCharacterSlug() {
		assertThatThrownBy(() -> TenantSchemaName.fromSlug("a")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullSlug() {
		assertThatThrownBy(() -> TenantSchemaName.fromSlug(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectValueWithoutTenantPrefix() {
		assertThatThrownBy(() -> new TenantSchemaName("acme")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullValue() {
		assertThatThrownBy(() -> new TenantSchemaName(null)).isInstanceOf(IllegalArgumentException.class);
	}
}
