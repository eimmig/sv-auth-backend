package com.stakevault.betting.auth.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextHolder;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.TelegramLinkStatus;
import com.stakevault.betting.auth.domain.port.in.GetTelegramLinkStatusUseCase;
import com.stakevault.betting.auth.domain.port.out.TelegramAccountRepository;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkRepository;

@Service
public class GetTelegramLinkStatusService implements GetTelegramLinkStatusUseCase {

	private final TelegramAccountRepository telegramAccountRepository;
	private final TelegramLinkRepository telegramLinkRepository;

	public GetTelegramLinkStatusService(TelegramAccountRepository telegramAccountRepository,
			TelegramLinkRepository telegramLinkRepository) {
		this.telegramAccountRepository = telegramAccountRepository;
		this.telegramLinkRepository = telegramLinkRepository;
	}

	@Override
	public TelegramLinkStatus statusOf(UUID callerId) {
		var schema = TenantContextHolder.current();
		if (schema == null) {
			throw new MissingTenantContextException();
		}
		return telegramAccountRepository.findByUserId(callerId)
				.filter(account -> telegramLinkRepository.findByTelegramUserId(account.telegramUserId())
						.filter(link -> link.tenantSlug().equals(schema.slug()) && link.userId().equals(callerId))
						.isPresent())
				.map(account -> new TelegramLinkStatus(true, account.linkedAt()))
				.orElseGet(TelegramLinkStatus::notLinked);
	}
}
