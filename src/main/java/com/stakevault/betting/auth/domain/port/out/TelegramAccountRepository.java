package com.stakevault.betting.auth.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.stakevault.betting.auth.domain.model.TelegramAccount;

public interface TelegramAccountRepository {

	TelegramAccount save(TelegramAccount telegramAccount);

	Optional<TelegramAccount> findByUserId(UUID userId);

	Optional<TelegramAccount> findByTelegramUserId(String telegramUserId);
}
