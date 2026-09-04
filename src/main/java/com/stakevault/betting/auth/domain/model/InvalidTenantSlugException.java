package com.stakevault.betting.auth.domain.model;

public class InvalidTenantSlugException extends SlugRelatedDomainException {

	public InvalidTenantSlugException(String slug, Throwable cause) {
		super("invalid tenant slug: " + slug, slug, cause);
	}

	@Override
	public String messageKey() {
		return "error.invalid-tenant-slug";
	}

	@Override
	public int httpStatusCode() {
		return 422;
	}
}
