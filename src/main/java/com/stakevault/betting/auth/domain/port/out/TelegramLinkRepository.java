package com.stakevault.betting.auth.domain.port.out;

import java.util.Optional;

import com.stakevault.betting.auth.domain.model.TelegramLink;

public interface TelegramLinkRepository {

	void upsert(TelegramLink telegramLink);

	Optional<TelegramLink> findByTelegramUserId(String telegramUserId);
}
