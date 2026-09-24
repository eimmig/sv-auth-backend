package com.stakevault.betting.auth.domain.model;

public class UserNotFoundException extends LocalizedRuntimeException {

	public UserNotFoundException() {
		super("user does not exist in the resolved tenant");
	}

	@Override
	public String messageKey() {
		return "error.user-not-found";
	}

	@Override
	public int httpStatusCode() {
		return 404;
	}
}
