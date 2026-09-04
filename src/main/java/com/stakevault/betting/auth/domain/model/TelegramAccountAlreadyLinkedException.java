package com.stakevault.betting.auth.domain.model;

public class TelegramAccountAlreadyLinkedException extends RuntimeException implements LocalizedDomainException {

	public TelegramAccountAlreadyLinkedException() {
		super("telegram account already linked");
	}

	@Override
	public String messageKey() {
		return "error.telegram-account-already-linked";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}
}
