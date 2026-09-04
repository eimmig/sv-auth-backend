package com.stakevault.betting.auth.domain.model;

public class TenantAlreadyProvisionedException extends RuntimeException implements LocalizedDomainException {

	private final String slug;

	public TenantAlreadyProvisionedException(String slug) {
		super("tenant already provisioned: " + slug);
		this.slug = slug;
	}

	@Override
	public String messageKey() {
		return "error.tenant-already-provisioned";
	}

	@Override
	public int httpStatusCode() {
		return 409;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { slug };
	}

	public String slug() {
		return slug;
	}
}
