package com.stakevault.betting.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.DownstreamProvisioningException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.DownstreamTenantProvisioner;
import com.stakevault.betting.auth.domain.port.out.UserRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrganizationControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	@MockitoBean
	private DownstreamTenantProvisioner downstream;

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final UserRepository userRepository;

	OrganizationControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema,
			JdbcTemplate jdbcTemplate, UserRepository userRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.userRepository = userRepository;
	}

	private User seedUser(Role role) {
		User user = new User(UUID.randomUUID(), "Seed", role + "@" + tenantSlug, "hash", role, false,
				Instant.now().truncatedTo(DB_PRECISION));
		try (var _ = TenantContextScope.open(schema)) {
			userRepository.save(user);
		}
		return user;
	}

	private HttpResponse<String> deleteOrganization(UUID callerId) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/organization"))
				.header("X-Tenant-Id", tenantSlug)
				.header("X-User-Id", callerId.toString())
				.header("Accept-Language", "pt-BR")
				.DELETE()
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private boolean schemaExists() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?", Integer.class,
				schema.value());
		return count != null && count > 0;
	}

	private int telegramLinksOfTenant() {
		Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM public.telegram_link WHERE tenant_id = ?",
				Integer.class, tenantSlug);
		return count == null ? 0 : count;
	}

	@Test
	void shouldDeleteDownstreamDataThenTheAuthSchemaAndTelegramLinksWhenCallerIsAdmin() throws Exception {
		User admin = seedUser(Role.ADMIN);
		jdbcTemplate.update("INSERT INTO public.telegram_link (telegram_user_id, tenant_id, user_id) VALUES (?, ?, ?)",
				"tg-" + tenantSlug, tenantSlug, admin.id());
		jdbcTemplate.update("INSERT INTO public.telegram_link (telegram_user_id, tenant_id, user_id) VALUES (?, ?, ?)",
				"tg-outra-" + tenantSlug, "outra-org", UUID.randomUUID());

		HttpResponse<String> response = deleteOrganization(admin.id());

		try {
			assertThat(response.statusCode()).isEqualTo(204);
			InOrder order = inOrder(downstream);
			order.verify(downstream).deleteFromBetsService(tenantSlug);
			order.verify(downstream).deleteFromStatsService(tenantSlug);
			assertThat(schemaExists()).isFalse();
			assertThat(telegramLinksOfTenant()).isZero();
			assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM public.telegram_link WHERE tenant_id = ?",
					Integer.class, "outra-org")).isEqualTo(1);
		} finally {
			jdbcTemplate.update("DELETE FROM public.telegram_link WHERE telegram_user_id = ?", "tg-outra-" + tenantSlug);
		}
	}

	@Test
	void shouldRefuseAMemberAndDeleteNothing() throws Exception {
		User member = seedUser(Role.MEMBER);

		HttpResponse<String> response = deleteOrganization(member.id());

		assertThat(response.statusCode()).isEqualTo(403);
		verify(downstream, never()).deleteFromBetsService(anyString());
		assertThat(schemaExists()).isTrue();
	}

	@Test
	void shouldAnswer502AndKeepTheAuthDataWhenADownstreamServiceFails() throws Exception {
		User admin = seedUser(Role.ADMIN);
		doThrow(new DownstreamProvisioningException("stats-service", new RuntimeException("connection refused")))
				.when(downstream).deleteFromStatsService(tenantSlug);

		HttpResponse<String> response = deleteOrganization(admin.id());

		assertThat(response.statusCode()).isEqualTo(502);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/organization-deletion-failed\"");
		assertThat(response.body()).contains("Organização não excluída");
		assertThat(schemaExists()).isTrue();
	}
}
