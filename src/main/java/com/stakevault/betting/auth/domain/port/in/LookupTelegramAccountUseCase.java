package com.stakevault.betting.auth.domain.port.in;

import com.stakevault.betting.auth.domain.model.TelegramLink;

public interface LookupTelegramAccountUseCase {

	TelegramLink lookup(String telegramUserId);
}
