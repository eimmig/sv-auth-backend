package com.stakevault.betting.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PendingTelegramLink(
		String code,
		String tenantSlug,
		UUID userId,
		Instant expiresAt) {

	public PendingTelegramLink {
		if (code == null || code.isBlank() || tenantSlug == null || tenantSlug.isBlank() || userId == null
				|| expiresAt == null) {
			throw new IllegalArgumentException("dados de vinculo Telegram pendente invalidos");
		}
	}

	public boolean isExpired(Instant now) {
		return !expiresAt.isAfter(now);
	}
}
