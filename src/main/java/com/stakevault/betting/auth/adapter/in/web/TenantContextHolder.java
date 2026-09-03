package com.stakevault.betting.auth.adapter.in.web;

import com.stakevault.betting.auth.domain.model.TenantSchemaName;

/** Schema do tenant resolvido para a requisicao atual. Preenchido por TenantSchemaFilter. */
public final class TenantContextHolder {

	private static final ThreadLocal<TenantSchemaName> CURRENT = new ThreadLocal<>();

	private TenantContextHolder() {
	}

	static void set(TenantSchemaName schema) {
		CURRENT.set(schema);
	}

	static void clear() {
		CURRENT.remove();
	}

	public static TenantSchemaName current() {
		return CURRENT.get();
	}
}
