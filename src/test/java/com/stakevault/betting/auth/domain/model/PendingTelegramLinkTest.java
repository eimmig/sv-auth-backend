package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PendingTelegramLinkTest {

	@Test
	void shouldAcceptValidData() {
		Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);
		PendingTelegramLink pending = new PendingTelegramLink("ABC12345", "acme", UUID.randomUUID(), expiresAt);

		assertThat(pending.code()).isEqualTo("ABC12345");
		assertThat(pending.expiresAt()).isEqualTo(expiresAt);
	}

	@Test
	void shouldRejectBlankCode() {
		UUID userId = UUID.randomUUID();
		Instant expiresAt = Instant.now().plusSeconds(60);

		assertThatThrownBy(() -> new PendingTelegramLink(" ", "acme", userId, expiresAt))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullExpiresAt() {
		UUID userId = UUID.randomUUID();

		assertThatThrownBy(() -> new PendingTelegramLink("ABC12345", "acme", userId, null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldBeExpiredWhenNowIsAfterExpiresAt() {
		PendingTelegramLink pending = new PendingTelegramLink("ABC12345", "acme", UUID.randomUUID(),
				Instant.now().minusSeconds(1));

		assertThat(pending.isExpired(Instant.now())).isTrue();
	}

	@Test
	void shouldNotBeExpiredWhenNowIsBeforeExpiresAt() {
		PendingTelegramLink pending = new PendingTelegramLink("ABC12345", "acme", UUID.randomUUID(),
				Instant.now().plusSeconds(60));

		assertThat(pending.isExpired(Instant.now())).isFalse();
	}
}
