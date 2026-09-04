package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TelegramAccountTest {

	@Test
	void shouldAcceptValidData() {
		TelegramAccount account = new TelegramAccount(UUID.randomUUID(), UUID.randomUUID(), "123456", Instant.now());

		assertThat(account.telegramUserId()).isEqualTo("123456");
	}

	@Test
	void shouldRejectNullUserId() {
		UUID id = UUID.randomUUID();
		Instant now = Instant.now();

		assertThatThrownBy(() -> new TelegramAccount(id, null, "123456", now))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectBlankTelegramUserId() {
		UUID id = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();

		assertThatThrownBy(() -> new TelegramAccount(id, userId, " ", now))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
