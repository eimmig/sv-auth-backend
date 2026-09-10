package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.config.TenantContextHolder;
import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.model.TelegramLinkCodeExpiredException;
import com.stakevault.betting.auth.domain.model.TelegramLinkCodeNotFoundException;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;

class ConfirmTelegramLinkServiceTest {

	private final PendingTelegramLinkRepository pendingTelegramLinkRepository = mock(PendingTelegramLinkRepository.class);
	private final TelegramLinkConfirmationTransaction confirmationTransaction = mock(TelegramLinkConfirmationTransaction.class);
	private final ConfirmTelegramLinkService service = new ConfirmTelegramLinkService(pendingTelegramLinkRepository,
			confirmationTransaction);

	@Test
	void shouldRejectWhenCodeDoesNotExist() {
		when(pendingTelegramLinkRepository.findByCode("ABC12345")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.confirm("111", "ABC12345"))
				.isInstanceOf(TelegramLinkCodeNotFoundException.class);
	}

	@Test
	void shouldDeleteAndRejectWhenCodeIsExpired() {
		PendingTelegramLink expired = new PendingTelegramLink("ABC12345", "acme", UUID.randomUUID(),
				Instant.now().minusSeconds(1));
		when(pendingTelegramLinkRepository.findByCode("ABC12345")).thenReturn(Optional.of(expired));

		assertThatThrownBy(() -> service.confirm("111", "ABC12345"))
				.isInstanceOf(TelegramLinkCodeExpiredException.class);

		verify(pendingTelegramLinkRepository).deleteByCode("ABC12345");
		verify(confirmationTransaction, never()).execute(any(), any());
	}

	@Test
	void shouldOpenTenantScopeAndDelegateToTransactionWhenCodeIsValid() {
		UUID userId = UUID.randomUUID();
		PendingTelegramLink pending = new PendingTelegramLink("ABC12345", "acme", userId, Instant.now().plusSeconds(60));
		when(pendingTelegramLinkRepository.findByCode("ABC12345")).thenReturn(Optional.of(pending));
		doAnswer(invocation -> {
			assertThat(TenantContextHolder.current().value()).isEqualTo("tenant_acme");
			return null;
		}).when(confirmationTransaction).execute(pending, "111");

		service.confirm("111", "ABC12345");

		verify(confirmationTransaction).execute(pending, "111");
		assertThat(TenantContextHolder.current()).isNull();
	}
}
