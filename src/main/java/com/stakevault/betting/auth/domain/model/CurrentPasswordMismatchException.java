package com.stakevault.betting.auth.domain.model;

public class CurrentPasswordMismatchException extends RuntimeException implements LocalizedDomainException {

	public CurrentPasswordMismatchException() {
		super("current password does not match");
	}

	@Override
	public String messageKey() {
		return "error.current-password-mismatch";
	}

	@Override
	public int httpStatusCode() {
		return 401;
	}
}
