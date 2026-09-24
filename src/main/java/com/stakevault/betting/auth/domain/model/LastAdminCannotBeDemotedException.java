package com.stakevault.betting.auth.domain.model;

public class LastAdminCannotBeDemotedException extends LocalizedRuntimeException {

	public LastAdminCannotBeDemotedException() {
		super("cannot demote the last remaining admin of the tenant");
	}

	@Override
	public String messageKey() {
		return "error.last-admin-cannot-be-demoted";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}
}
