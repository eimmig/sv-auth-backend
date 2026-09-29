package com.stakevault.betting.auth.domain.model;

public class TenantAlreadyProvisionedException extends LocalizedRuntimeException {

	private final String slug;

	public TenantAlreadyProvisionedException(String slug) {
		super("tenant already provisioned: " + slug, (Throwable) null, slug == null ? "" : slug);
		this.slug = slug;
	}

	public String slug() {
		return slug;
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
