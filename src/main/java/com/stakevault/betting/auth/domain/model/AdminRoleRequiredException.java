package com.stakevault.betting.auth.domain.model;

public class AdminRoleRequiredException extends RuntimeException implements LocalizedDomainException {

	public AdminRoleRequiredException() {
		super("caller is not an admin of the resolved tenant");
	}

	@Override
	public String messageKey() {
		return "error.admin-role-required";
	}

	@Override
	public int httpStatusCode() {
		return 403;
	}
}
