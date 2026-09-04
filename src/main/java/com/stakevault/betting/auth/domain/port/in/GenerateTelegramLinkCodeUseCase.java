package com.stakevault.betting.auth.domain.port.in;

import java.util.UUID;

import com.stakevault.betting.auth.domain.model.GeneratedTelegramLinkCode;

public interface GenerateTelegramLinkCodeUseCase {

	GeneratedTelegramLinkCode generateCode(UUID callerId);
}
