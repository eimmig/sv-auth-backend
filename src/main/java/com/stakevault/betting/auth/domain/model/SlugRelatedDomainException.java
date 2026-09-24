package com.stakevault.betting.auth.domain.model;

abstract class SlugRelatedDomainException extends LocalizedRuntimeException {

	private final String slug;

	protected SlugRelatedDomainException(String message, String slug, Throwable cause) {
		super(message, cause, slug == null ? "" : slug);
		this.slug = slug;
	}

	public String slug() {
		return slug;
	}
}
