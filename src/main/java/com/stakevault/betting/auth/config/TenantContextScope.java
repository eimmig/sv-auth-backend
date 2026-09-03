package com.stakevault.betting.auth.config;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;

/** Unica forma externa de mutar TenantContextHolder - abre no request, fecha (limpa) via try-with-resources. */
public final class TenantContextScope implements AutoCloseable {

	private TenantContextScope() {
	}

	public static TenantContextScope open(TenantSchemaName schema) {
		TenantContextHolder.set(schema);
		return new TenantContextScope();
	}

	@Override
	public void close() {
		TenantContextHolder.clear();
	}
}
