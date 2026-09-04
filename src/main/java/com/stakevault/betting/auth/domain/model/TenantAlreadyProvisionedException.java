package com.stakevault.betting.auth.domain.model;

public class TenantAlreadyProvisionedException extends SlugRelatedDomainException {

	public TenantAlreadyProvisionedException(String slug) {
		super("tenant already provisioned: " + slug, slug, null);
	}

	@Override
	public String messageKey() {
		return "error.tenant-already-provisioned";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}
}
