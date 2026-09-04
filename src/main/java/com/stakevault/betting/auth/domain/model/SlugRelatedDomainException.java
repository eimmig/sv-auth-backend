package com.stakevault.betting.auth.domain.model;

abstract class SlugRelatedDomainException extends RuntimeException implements LocalizedDomainException {

	private final String slug;

	protected SlugRelatedDomainException(String message, String slug, Throwable cause) {
		super(message, cause);
		this.slug = slug;
	}

	@Override
	public Object[] messageArgs() {
		return new Object[] { slug == null ? "" : slug };
	}

	public String slug() {
		return slug;
	}
}
