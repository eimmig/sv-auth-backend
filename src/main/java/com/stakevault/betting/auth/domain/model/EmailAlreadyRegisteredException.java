package com.stakevault.betting.auth.domain.model;

public class EmailAlreadyRegisteredException extends RuntimeException implements LocalizedDomainException {

	private final String email;

	public EmailAlreadyRegisteredException(String email) {
		super("email already registered in tenant: " + email);
		this.email = email;
	}

	@Override
	public String messageKey() {
		return "error.email-already-registered";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { email };
	}
}
