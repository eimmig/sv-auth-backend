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
import com.stakevault.betting.auth.domain.port.out.PasswordHasher;
import com.stakevault.betting.auth.domain.port.out.UserRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	private static final String RAW_PASSWORD = "correct-horse-battery-staple";

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;

	AuthControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			UserRepository userRepository, PasswordHasher passwordHasher) {
		super(provisionTenantSchema, jdbcTemplate);
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
	}

	private User seedUser(boolean mustChangePassword) {
		User user = new User(UUID.randomUUID(), "Ana", "ana@" + tenantSlug, passwordHasher.hash(RAW_PASSWORD),
				Role.MEMBER, mustChangePassword, Instant.now().truncatedTo(DB_PRECISION));
		try (var _ = TenantContextScope.open(schema)) {
			userRepository.save(user);
		}
		return user;
	}

	private HttpResponse<String> post(String body, String... headers) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/login"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		for (int i = 0; i < headers.length; i += 2) {
			builder.header(headers[i], headers[i + 1]);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void shouldReturnTokenAndMustChangePasswordOnValidLogin() throws Exception {
		seedUser(true);

		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\",\"email\":\"ana@" + tenantSlug + "\",\"password\":\"" + RAW_PASSWORD + "\"}");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"token\":\"v4.local.");
		assertThat(response.body()).contains("\"mustChangePassword\":true");
	}

	@Test
	void shouldReturn401WhenTenantDoesNotExist() throws Exception {
		String unknownSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		HttpResponse<String> response = post(
				"{\"slug\":\"" + unknownSlug + "\",\"email\":\"ana@" + unknownSlug + "\",\"password\":\"" + RAW_PASSWORD + "\"}");

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-credentials\"");
	}

	@Test
	void shouldReturn401WhenEmailDoesNotExist() throws Exception {
		seedUser(false);

		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\",\"email\":\"nobody@" + tenantSlug + "\",\"password\":\"" + RAW_PASSWORD + "\"}");

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-credentials\"");
	}

	@Test
	void shouldReturn401WhenPasswordIsWrong() throws Exception {
		seedUser(false);

		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\",\"email\":\"ana@" + tenantSlug + "\",\"password\":\"wrong-password\"}");

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-credentials\"");
	}

	@Test
	void shouldReturnSameBodyForAllFourInvalidCredentialsCases() throws Exception {
		seedUser(false);
		String unknownSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		HttpResponse<String> invalidSlug = post(
				"{\"slug\":\"1invalid\",\"email\":\"ana@" + tenantSlug + "\",\"password\":\"" + RAW_PASSWORD + "\"}");
		HttpResponse<String> unknownTenant = post(
				"{\"slug\":\"" + unknownSlug + "\",\"email\":\"ana@" + tenantSlug + "\",\"password\":\"" + RAW_PASSWORD + "\"}");
		HttpResponse<String> unknownEmail = post(
				"{\"slug\":\"" + tenantSlug + "\",\"email\":\"nobody@" + tenantSlug + "\",\"password\":\"" + RAW_PASSWORD + "\"}");
		HttpResponse<String> wrongPassword = post(
				"{\"slug\":\"" + tenantSlug + "\",\"email\":\"ana@" + tenantSlug + "\",\"password\":\"wrong\"}");

		assertThat(invalidSlug.statusCode()).isEqualTo(401);
		assertThat(unknownTenant.statusCode()).isEqualTo(401);
		assertThat(unknownEmail.statusCode()).isEqualTo(401);
		assertThat(wrongPassword.statusCode()).isEqualTo(401);
		assertThat(invalidSlug.body().replaceAll("\"instance\":\"[^\"]*\"", ""))
				.isEqualTo(unknownTenant.body().replaceAll("\"instance\":\"[^\"]*\"", ""))
				.isEqualTo(unknownEmail.body().replaceAll("\"instance\":\"[^\"]*\"", ""))
				.isEqualTo(wrongPassword.body().replaceAll("\"instance\":\"[^\"]*\"", ""));
	}

	@Test
	void shouldReturn400WhenPayloadIsInvalid() throws Exception {
		HttpResponse<String> response = post("{\"slug\":\"\",\"email\":\"\",\"password\":\"\"}");

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/validation-failed\"");
	}

	@Test
	void shouldLocalizeErrorTitleAndDetailPerAcceptLanguage() throws Exception {
		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\",\"email\":\"nobody@" + tenantSlug + "\",\"password\":\"" + RAW_PASSWORD + "\"}",
				"Accept-Language", "es");

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("Credenciales inválidas");
	}
}
