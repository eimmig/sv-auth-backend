package com.stakevault.betting.auth.domain.model;

import java.time.Instant;

public record TelegramLinkStatus(boolean linked, Instant linkedAt) {

	public static TelegramLinkStatus notLinked() {
		return new TelegramLinkStatus(false, null);
	}
}
