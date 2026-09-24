package com.stakevault.betting.auth.domain.model;

public class EmailAlreadyRegisteredException extends LocalizedRuntimeException {

	public EmailAlreadyRegisteredException(String email) {
		super("email already registered in tenant: " + email, email);
	}

	@Override
	public String messageKey() {
		return "error.email-already-registered";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}
}
