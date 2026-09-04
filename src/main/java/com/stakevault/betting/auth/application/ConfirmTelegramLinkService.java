package com.stakevault.betting.auth.application;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.model.TelegramLinkCodeExpiredException;
import com.stakevault.betting.auth.domain.model.TelegramLinkCodeNotFoundException;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.port.in.ConfirmTelegramLinkUseCase;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;

@Service
public class ConfirmTelegramLinkService implements ConfirmTelegramLinkUseCase {

	private final PendingTelegramLinkRepository pendingTelegramLinkRepository;
	private final TelegramLinkConfirmationTransaction confirmationTransaction;

	public ConfirmTelegramLinkService(PendingTelegramLinkRepository pendingTelegramLinkRepository,
			TelegramLinkConfirmationTransaction confirmationTransaction) {
		this.pendingTelegramLinkRepository = pendingTelegramLinkRepository;
		this.confirmationTransaction = confirmationTransaction;
	}

	@Override
	public void confirm(String telegramUserId, String code) {
		PendingTelegramLink pending = pendingTelegramLinkRepository.findByCode(code)
				.orElseThrow(TelegramLinkCodeNotFoundException::new);

		if (pending.isExpired(Instant.now())) {
			pendingTelegramLinkRepository.deleteByCode(code);
			throw new TelegramLinkCodeExpiredException();
		}

		TenantSchemaName schema = TenantSchemaName.fromSlug(pending.tenantSlug());
		try (var _ = TenantContextScope.open(schema)) {
			confirmationTransaction.execute(pending, telegramUserId);
		}
	}
}
