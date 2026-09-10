package com.stakevault.betting.auth.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.model.TelegramAccount;
import com.stakevault.betting.auth.domain.model.TelegramAccountAlreadyLinkedException;
import com.stakevault.betting.auth.domain.model.TelegramLink;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;
import com.stakevault.betting.auth.domain.port.out.TelegramAccountRepository;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkRepository;

@Component
public class TelegramLinkConfirmationTransaction {

	private final TelegramAccountRepository telegramAccountRepository;
	private final TelegramLinkRepository telegramLinkRepository;
	private final PendingTelegramLinkRepository pendingTelegramLinkRepository;

	public TelegramLinkConfirmationTransaction(TelegramAccountRepository telegramAccountRepository,
			TelegramLinkRepository telegramLinkRepository, PendingTelegramLinkRepository pendingTelegramLinkRepository) {
		this.telegramAccountRepository = telegramAccountRepository;
		this.telegramLinkRepository = telegramLinkRepository;
		this.pendingTelegramLinkRepository = pendingTelegramLinkRepository;
	}

	@Transactional
	public void execute(PendingTelegramLink pending, String telegramUserId) {
		boolean alreadyLinked = telegramAccountRepository.findByUserId(pending.userId()).isPresent()
				|| telegramAccountRepository.findByTelegramUserId(telegramUserId).isPresent();
		if (alreadyLinked) {
			throw new TelegramAccountAlreadyLinkedException();
		}

		telegramAccountRepository
				.save(new TelegramAccount(UUID.randomUUID(), pending.userId(), telegramUserId, Instant.now()));
		telegramLinkRepository.upsert(new TelegramLink(telegramUserId, pending.tenantSlug(), pending.userId()));
		pendingTelegramLinkRepository.deleteByCode(pending.code());
	}
}
