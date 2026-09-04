package com.stakevault.betting.auth.domain.port.in;

public interface ConfirmTelegramLinkUseCase {

	void confirm(String telegramUserId, String code);
}
