package com.stakevault.betting.auth.domain.model;

public class InvalidCredentialsException extends LocalizedRuntimeException {

	public InvalidCredentialsException() {
		super("invalid tenant slug, email or password");
	}

	@Override
	public String messageKey() {
		return "error.invalid-credentials";
	}

	@Override
	public int httpStatusCode() {
		return 401;
	}
}
