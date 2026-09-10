package com.stakevault.betting.auth.domain.model;

public class TelegramAccountNotFoundException extends RuntimeException implements LocalizedDomainException {

	public TelegramAccountNotFoundException() {
		super("telegram account not linked");
	}

	@Override
	public String messageKey() {
		return "error.telegram-account-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
