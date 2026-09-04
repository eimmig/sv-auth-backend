package com.stakevault.betting.auth.domain.model;

import java.util.regex.Pattern;

public record TenantSchemaName(String value) {

	private static final Pattern SLUG = Pattern.compile("^[a-z][a-z0-9-]{1,55}$");

	public TenantSchemaName {
		if (value == null || !value.startsWith("tenant_")) {
			throw new IllegalArgumentException("nome de schema de tenant invalido: " + value);
		}
	}

	public static TenantSchemaName fromSlug(String slug) {
		if (slug == null || !SLUG.matcher(slug).matches()) {
			throw new IllegalArgumentException("slug de tenant invalido: " + slug);
		}
		return new TenantSchemaName("tenant_" + slug);
	}

	public String slug() {
		return value.substring("tenant_".length());
	}
}
