package com.stakevault.betting.auth.domain.model;

import java.util.UUID;

public record TelegramLink(
		String telegramUserId,
		String tenantSlug,
		UUID userId) {

	public TelegramLink {
		if (telegramUserId == null || telegramUserId.isBlank() || tenantSlug == null || tenantSlug.isBlank()
				|| userId == null) {
			throw new IllegalArgumentException("dados de vinculo Telegram invalidos");
		}
	}
}
