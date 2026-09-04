package com.stakevault.betting.auth.domain.model;

public class CallerNotFoundException extends RuntimeException implements LocalizedDomainException {

	public CallerNotFoundException() {
		super("caller does not exist in the current tenant");
	}

	@Override
	public String messageKey() {
		return "error.caller-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 401;
	}
}
