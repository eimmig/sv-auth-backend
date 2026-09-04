package com.stakevault.betting.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.UserRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsersControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final UserRepository userRepository;

	UsersControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			UserRepository userRepository) {
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

	private HttpResponse<String> post(String body, String... headers) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/users"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		for (int i = 0; i < headers.length; i += 2) {
			builder.header(headers[i], headers[i + 1]);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void shouldCreateMemberUserWhenCallerIsAdmin() throws Exception {
		User admin = seedUser(Role.ADMIN);

		HttpResponse<String> response = post(
				"{\"name\":\"New Member\",\"email\":\"member@" + tenantSlug + "\",\"password\":\"raw-password\"}",
				"X-Tenant-Id", tenantSlug, "X-User-Id", admin.id().toString());

		assertThat(response.statusCode()).isEqualTo(201);
		assertThat(response.body()).contains("\"role\":\"MEMBER\"");
		assertThat(response.body()).contains("\"mustChangePassword\":false");
		assertThat(response.body()).doesNotContain("passwordHash").doesNotContain("raw-password");
	}

	@Test
	void shouldReturn401WhenCallerHeaderIsMissing() throws Exception {
		HttpResponse<String> response = post(
				"{\"name\":\"New Member\",\"email\":\"member@" + tenantSlug + "\",\"password\":\"raw-password\"}",
				"X-Tenant-Id", tenantSlug);

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/missing-caller-context\"");
	}

	@Test
	void shouldReturn401WhenCallerHeaderIsNotAValidUuid() throws Exception {
		HttpResponse<String> response = post(
				"{\"name\":\"New Member\",\"email\":\"member@" + tenantSlug + "\",\"password\":\"raw-password\"}",
				"X-Tenant-Id", tenantSlug, "X-User-Id", "not-a-uuid");

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void shouldReturn400WhenTenantHeaderIsMissing() throws Exception {
		HttpResponse<String> response = post(
				"{\"name\":\"New Member\",\"email\":\"member@example.com\",\"password\":\"raw-password\"}",
				"X-User-Id", UUID.randomUUID().toString());

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/missing-tenant-context\"");
	}

	@Test
	void shouldReturn403WhenCallerIsNotAdmin() throws Exception {
		User member = seedUser(Role.MEMBER);

		HttpResponse<String> response = post(
				"{\"name\":\"New Member\",\"email\":\"other@" + tenantSlug + "\",\"password\":\"raw-password\"}",
				"X-Tenant-Id", tenantSlug, "X-User-Id", member.id().toString());

		assertThat(response.statusCode()).isEqualTo(403);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/admin-role-required\"");
	}

	@Test
	void shouldReturn403WhenCallerDoesNotExistInTenant() throws Exception {
		HttpResponse<String> response = post(
				"{\"name\":\"New Member\",\"email\":\"other@" + tenantSlug + "\",\"password\":\"raw-password\"}",
				"X-Tenant-Id", tenantSlug, "X-User-Id", UUID.randomUUID().toString());

		assertThat(response.statusCode()).isEqualTo(403);
	}

	@Test
	void shouldReturn409WhenEmailAlreadyRegisteredInTenant() throws Exception {
		User admin = seedUser(Role.ADMIN);
		User existing = seedUser(Role.MEMBER);

		HttpResponse<String> response = post(
				"{\"name\":\"Duplicate\",\"email\":\"" + existing.email() + "\",\"password\":\"raw-password\"}",
				"X-Tenant-Id", tenantSlug, "X-User-Id", admin.id().toString());

		assertThat(response.statusCode()).isEqualTo(409);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/email-already-registered\"");
	}

	@Test
	void shouldReturn400WhenPayloadIsInvalid() throws Exception {
		User admin = seedUser(Role.ADMIN);

		HttpResponse<String> response = post(
				"{\"name\":\"\",\"email\":\"not-an-email\",\"password\":\"raw-password\"}",
				"X-Tenant-Id", tenantSlug, "X-User-Id", admin.id().toString());

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/validation-failed\"");
	}

	@Test
	void shouldLocalizeErrorTitleAndDetailPerAcceptLanguage() throws Exception {
		User member = seedUser(Role.MEMBER);

		HttpResponse<String> response = post(
				"{\"name\":\"New Member\",\"email\":\"other@" + tenantSlug + "\",\"password\":\"raw-password\"}",
				"X-Tenant-Id", tenantSlug, "X-User-Id", member.id().toString(), "Accept-Language", "es");

		assertThat(response.statusCode()).isEqualTo(403);
		assertThat(response.body()).contains("Se requiere rol de administrador");
	}
}
