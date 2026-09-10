package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.domain.model.TelegramAccountNotFoundException;
import com.stakevault.betting.auth.domain.model.TelegramLink;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkRepository;

class LookupTelegramAccountServiceTest {

	private final TelegramLinkRepository telegramLinkRepository = mock(TelegramLinkRepository.class);
	private final LookupTelegramAccountService service = new LookupTelegramAccountService(telegramLinkRepository);

	@Test
	void shouldReturnLinkWhenPresent() {
		TelegramLink link = new TelegramLink("111", "acme", UUID.randomUUID());
		when(telegramLinkRepository.findByTelegramUserId("111")).thenReturn(Optional.of(link));

		assertThat(service.lookup("111")).isEqualTo(link);
	}

	@Test
	void shouldRejectWhenNotFound() {
		when(telegramLinkRepository.findByTelegramUserId("111")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.lookup("111")).isInstanceOf(TelegramAccountNotFoundException.class);
	}
}
