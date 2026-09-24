package com.stakevault.betting.auth.domain.model;

public class TelegramLinkCodeExpiredException extends LocalizedRuntimeException {

	public TelegramLinkCodeExpiredException() {
		super("telegram link code expired");
	}

	@Override
	public String messageKey() {
		return "error.telegram-link-code-expired";
	}

	@Override
	public int httpStatusCode() {
		return 422;
	}
}
