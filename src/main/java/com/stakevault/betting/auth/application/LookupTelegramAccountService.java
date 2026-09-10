package com.stakevault.betting.auth.application;

import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.domain.model.TelegramAccountNotFoundException;
import com.stakevault.betting.auth.domain.model.TelegramLink;
import com.stakevault.betting.auth.domain.port.in.LookupTelegramAccountUseCase;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkRepository;

@Service
public class LookupTelegramAccountService implements LookupTelegramAccountUseCase {

	private final TelegramLinkRepository telegramLinkRepository;

	public LookupTelegramAccountService(TelegramLinkRepository telegramLinkRepository) {
		this.telegramLinkRepository = telegramLinkRepository;
	}

	@Override
	public TelegramLink lookup(String telegramUserId) {
		return telegramLinkRepository.findByTelegramUserId(telegramUserId)
				.orElseThrow(TelegramAccountNotFoundException::new);
	}
}
