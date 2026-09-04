package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.model.TelegramAccount;
import com.stakevault.betting.auth.domain.model.TelegramAccountAlreadyLinkedException;
import com.stakevault.betting.auth.domain.model.TelegramLink;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;
import com.stakevault.betting.auth.domain.port.out.TelegramAccountRepository;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkRepository;

class TelegramLinkConfirmationTransactionTest {

	private static final UUID USER_ID = UUID.randomUUID();

	private final TelegramAccountRepository telegramAccountRepository = mock(TelegramAccountRepository.class);
	private final TelegramLinkRepository telegramLinkRepository = mock(TelegramLinkRepository.class);
	private final PendingTelegramLinkRepository pendingTelegramLinkRepository = mock(PendingTelegramLinkRepository.class);
	private final TelegramLinkConfirmationTransaction transaction = new TelegramLinkConfirmationTransaction(
			telegramAccountRepository, telegramLinkRepository, pendingTelegramLinkRepository);

	private PendingTelegramLink pending() {
		return new PendingTelegramLink("ABC12345", "acme", USER_ID, Instant.now().plusSeconds(60));
	}

	@Test
	void shouldLinkAccountUpsertDirectoryAndDeletePendingCodeWhenNotAlreadyLinked() {
		when(telegramAccountRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
		when(telegramAccountRepository.findByTelegramUserId("111")).thenReturn(Optional.empty());

		transaction.execute(pending(), "111");

		verify(telegramAccountRepository).save(argThatMatches((TelegramAccount account) -> account.userId().equals(USER_ID)
				&& account.telegramUserId().equals("111")));
		verify(telegramLinkRepository).upsert(argThatMatches(
				(TelegramLink link) -> link.telegramUserId().equals("111") && link.tenantSlug().equals("acme")
						&& link.userId().equals(USER_ID)));
		verify(pendingTelegramLinkRepository).deleteByCode("ABC12345");
	}

	@Test
	void shouldRejectWhenUserAlreadyHasATelegramAccount() {
		when(telegramAccountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(
				new TelegramAccount(UUID.randomUUID(), USER_ID, "999", Instant.now())));
		PendingTelegramLink pending = pending();

		assertThatThrownBy(() -> transaction.execute(pending, "111"))
				.isInstanceOf(TelegramAccountAlreadyLinkedException.class);

		verify(telegramAccountRepository, never()).save(any());
		verify(telegramLinkRepository, never()).upsert(any());
		verify(pendingTelegramLinkRepository, never()).deleteByCode(any());
	}

	@Test
	void shouldRejectWhenTelegramUserIdAlreadyLinkedToAnotherAccountInTheSameTenant() {
		when(telegramAccountRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
		when(telegramAccountRepository.findByTelegramUserId("111")).thenReturn(Optional.of(
				new TelegramAccount(UUID.randomUUID(), UUID.randomUUID(), "111", Instant.now())));
		PendingTelegramLink pending = pending();

		assertThatThrownBy(() -> transaction.execute(pending, "111"))
				.isInstanceOf(TelegramAccountAlreadyLinkedException.class);

		verify(telegramAccountRepository, never()).save(any());
	}

	private <T> T argThatMatches(Predicate<T> predicate) {
		return argThat(predicate::test);
	}
}
