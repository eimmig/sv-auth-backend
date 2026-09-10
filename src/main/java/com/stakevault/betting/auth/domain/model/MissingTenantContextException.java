package com.stakevault.betting.auth.domain.model;

public class MissingTenantContextException extends RuntimeException implements LocalizedDomainException {

	public MissingTenantContextException() {
		super("missing X-Tenant-Id");
	}

	@Override
	public String messageKey() {
		return "error.missing-tenant-context";
	}

	@Override
	public int httpStatusCode() {
		return 400;
	}
}
