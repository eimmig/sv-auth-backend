package com.stakevault.betting.auth.adapter.in.web;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.TenantSchemaNotFoundException;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Resolve X-Tenant-Id, garante que o schema esta em dia (migracao lazy, nunca cria - ver
 * ProvisionTenantSchemaUseCase) e disponibiliza o schema resolvido via TenantContextHolder
 * antes do controller. Requisicao sem o header passa direto (rotas sem tenant, ex.:
 * actuator, ou rotas admin autenticadas por X-Admin-Api-Key - ver docs/API-CONTRACTS.md).
 *
 * Tambem injeta tenantId no MDC (docs/OBSERVABILITY-AND-CONFIG.md) para toda linha de
 * log da requisicao aparecer com o tenant - correlationId fica para o api-gateway
 * (feat-008), que e quem atribui/propaga esse id.
 *
 * Erros ja em application/problem+json (RFC 7807, ver docs/API-CONTRACTS.md) - title/detail
 * fixos em pt-BR ate o MessageSource de feat-001.5 existir para localizar por
 * Accept-Language; `type` e `status` ja sao definitivos.
 *
 * Limitacao conhecida, nao endereçada aqui: OncePerRequestFilter roda de novo em
 * dispatch assincrono por padrao (shouldNotFilterAsyncDispatch=true so evita RE-filtrar,
 * nao evita o finally abaixo limpar o ThreadLocal antes do trabalho assincrono
 * terminar). Sem risco hoje porque nenhum endpoint deste servico usa
 * startAsync/DeferredResult/Callable - revisitar se algum vier a usar.
 */
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
			// Sem schema valido, nao ha tenantId para por no MDC - so o slug bruto do
			// header, que ja aparece implicito na propria mensagem de erro abaixo.
			writeProblem(response, request, HttpServletResponse.SC_BAD_REQUEST,
					"invalid-tenant-id", "Tenant invalido", "O header X-Tenant-Id nao e um slug de tenant valido.");
			return;
		}

		// A partir daqui ja existe um schema valido - MDC cobre inclusive a resposta de
		// tenant-not-found abaixo, nao so o caminho feliz. Isso e o que o code review
		// pegou na primeira versao: MDC.put so depois do migrateIfPending deixava de
		// fora exatamente o log mais util (tentativa de tenant que nao existe).
		MDC.put(TENANT_MDC_KEY, schema.value());
		try {
			provisionTenantSchema.migrateIfPending(tenantSlug);
		} catch (TenantSchemaNotFoundException _) {
			writeProblem(response, request, HttpServletResponse.SC_NOT_FOUND,
					"tenant-not-found", "Tenant nao encontrado", "Nenhum tenant provisionado para o X-Tenant-Id informado.");
			MDC.remove(TENANT_MDC_KEY);
			return;
		}

		TenantContextHolder.set(schema);
		try {
			chain.doFilter(request, response);
		} finally {
			TenantContextHolder.clear();
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
