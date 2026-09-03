package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TelegramAccountTest {

	@Test
	void construtorAceitaDadosValidos() {
		TelegramAccount account = new TelegramAccount(UUID.randomUUID(), UUID.randomUUID(), "123456", Instant.now());

		assertThat(account.telegramUserId()).isEqualTo("123456");
	}

	@Test
	void rejeitaUserIdNulo() {
		assertThatThrownBy(() -> new TelegramAccount(UUID.randomUUID(), null, "123456", Instant.now()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rejeitaTelegramUserIdEmBranco() {
		assertThatThrownBy(() -> new TelegramAccount(UUID.randomUUID(), UUID.randomUUID(), " ", Instant.now()))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
