package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class TelegramLinkTest {

	@Test
	void shouldAcceptValidData() {
		TelegramLink link = new TelegramLink("123456", "acme", UUID.randomUUID());

		assertThat(link.telegramUserId()).isEqualTo("123456");
		assertThat(link.tenantSlug()).isEqualTo("acme");
	}

	@Test
	void shouldRejectBlankTelegramUserId() {
		assertThatThrownBy(() -> new TelegramLink(" ", "acme", UUID.randomUUID()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectBlankTenantSlug() {
		assertThatThrownBy(() -> new TelegramLink("123456", " ", UUID.randomUUID()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullUserId() {
		assertThatThrownBy(() -> new TelegramLink("123456", "acme", null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
