package com.stakevault.betting.auth.domain.model;

public class TelegramLinkCodeNotFoundException extends LocalizedRuntimeException {

	public TelegramLinkCodeNotFoundException() {
		super("telegram link code not found");
	}

	@Override
	public String messageKey() {
		return "error.telegram-link-code-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
