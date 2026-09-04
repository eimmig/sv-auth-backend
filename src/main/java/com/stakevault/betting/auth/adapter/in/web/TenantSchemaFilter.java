package com.stakevault.betting.auth.adapter.in.web;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;
import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.TenantSchemaNotFoundException;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantSchemaFilter extends OncePerRequestFilter {

	public static final String TENANT_HEADER = "X-Tenant-Id";
	private static final String TENANT_MDC_KEY = "tenantId";

	private final ProvisionTenantSchemaUseCase provisionTenantSchema;
	private final ObjectMapper objectMapper;

	public TenantSchemaFilter(ProvisionTenantSchemaUseCase provisionTenantSchema, ObjectMapper objectMapper) {
		this.provisionTenantSchema = provisionTenantSchema;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String tenantSlug = request.getHeader(TENANT_HEADER);
		if (tenantSlug == null || tenantSlug.isBlank()) {
			chain.doFilter(request, response);
			return;
		}

		TenantSchemaName schema;
		try {
			schema = TenantSchemaName.fromSlug(tenantSlug);
		} catch (IllegalArgumentException _) {
			writeProblem(response, request, HttpServletResponse.SC_BAD_REQUEST,
					"invalid-tenant-id", "Tenant invalido", "O header X-Tenant-Id nao e um slug de tenant valido.");
			return;
		}

		MDC.put(TENANT_MDC_KEY, schema.value());
		try {
			provisionTenantSchema.migrateIfPending(tenantSlug);
		} catch (TenantSchemaNotFoundException _) {
			writeProblem(response, request, HttpServletResponse.SC_NOT_FOUND,
					"tenant-not-found", "Tenant nao encontrado", "Nenhum tenant provisionado para o X-Tenant-Id informado.");
			MDC.remove(TENANT_MDC_KEY);
			return;
		}

		try (var _ = TenantContextScope.open(schema)) {
			chain.doFilter(request, response);
		} finally {
			MDC.remove(TENANT_MDC_KEY);
		}
	}

	private void writeProblem(HttpServletResponse response, HttpServletRequest request, int status,
			String typeSlug, String title, String detail) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		var body = new java.util.LinkedHashMap<String, Object>();
		body.put("type", "https://docs/errors/" + typeSlug);
		body.put("title", title);
		body.put("status", status);
		body.put("detail", detail);
		body.put("instance", request.getRequestURI());
		objectMapper.writeValue(response.getOutputStream(), body);
	}
}
