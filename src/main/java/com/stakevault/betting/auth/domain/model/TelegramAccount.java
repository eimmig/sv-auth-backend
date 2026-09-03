package com.stakevault.betting.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Vinculo 1:1 entre um USER e uma conta do Telegram, dentro do schema do tenant. */
public record TelegramAccount(
		UUID id,
		UUID userId,
		String telegramUserId,
		Instant linkedAt) {

	public TelegramAccount {
		if (id == null || userId == null || telegramUserId == null || telegramUserId.isBlank() || linkedAt == null) {
			throw new IllegalArgumentException("dados de vinculo Telegram invalidos");
		}
	}
}
