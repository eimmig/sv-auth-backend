package com.stakevault.betting.auth.domain.model;

public class InvalidTenantSlugException extends RuntimeException implements LocalizedDomainException {

	private final String slug;

	public InvalidTenantSlugException(String slug, Throwable cause) {
		super("invalid tenant slug: " + slug, cause);
		this.slug = slug;
	}

	@Override
	public String messageKey() {
		return "error.invalid-tenant-slug";
	}

	public String slug() {
		return slug;
	}
}
