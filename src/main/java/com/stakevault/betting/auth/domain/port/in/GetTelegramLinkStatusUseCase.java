package com.stakevault.betting.auth.domain.port.in;

import java.util.UUID;

import com.stakevault.betting.auth.domain.model.TelegramLinkStatus;

public interface GetTelegramLinkStatusUseCase {

	TelegramLinkStatus statusOf(UUID callerId);
}
