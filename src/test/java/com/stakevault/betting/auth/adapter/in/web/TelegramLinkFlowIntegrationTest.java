package com.stakevault.betting.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;
import com.stakevault.betting.auth.domain.port.out.UserRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TelegramLinkFlowIntegrationTest extends TenantSchemaIntegrationSupport {

	private static final Pattern CODE_PATTERN = Pattern.compile("\"code\":\"([^\"]+)\"");

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final UserRepository userRepository;
	private final PendingTelegramLinkRepository pendingTelegramLinkRepository;

	TelegramLinkFlowIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			UserRepository userRepository, PendingTelegramLinkRepository pendingTelegramLinkRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.userRepository = userRepository;
		this.pendingTelegramLinkRepository = pendingTelegramLinkRepository;
	}

	private User seedUser(String slug) {
		User user = new User(UUID.randomUUID(), "Seed", "seed-" + UUID.randomUUID() + "@" + slug, "hash", Role.MEMBER,
				false, Instant.now().truncatedTo(DB_PRECISION));
		try (var _ = TenantContextScope.open(TenantSchemaName.fromSlug(slug))) {
			userRepository.save(user);
		}
		return user;
	}

	private HttpResponse<String> generateCode(String tenantSlugValue, String callerId, String... extraHeaders)
			throws Exception {
		HttpRequest.Builder builder = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/telegram-links"))
				.POST(HttpRequest.BodyPublishers.noBody());
		if (tenantSlugValue != null) {
			builder.header("X-Tenant-Id", tenantSlugValue);
		}
		if (callerId != null) {
			builder.header("X-User-Id", callerId);
		}
		for (int i = 0; i < extraHeaders.length; i += 2) {
			builder.header(extraHeaders[i], extraHeaders[i + 1]);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> confirmLink(String telegramUserId, String code, String... extraHeaders)
			throws Exception {
		HttpRequest.Builder builder = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/telegram-accounts"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers
						.ofString("{\"telegramUserId\":\"" + telegramUserId + "\",\"code\":\"" + code + "\"}"));
		for (int i = 0; i < extraHeaders.length; i += 2) {
			builder.header(extraHeaders[i], extraHeaders[i + 1]);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> lookup(String telegramUserId) throws Exception {
		HttpRequest request = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + "/api/v1/telegram-accounts/" + telegramUserId))
				.GET().build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private String extractCode(String responseBody) {
		Matcher matcher = CODE_PATTERN.matcher(responseBody);
		if (!matcher.find()) {
			throw new IllegalStateException("no code in response: " + responseBody);
		}
		return matcher.group(1);
	}

	@Test
	void shouldGenerateConfirmAndLookupTelegramLink() throws Exception {
		User caller = seedUser(tenantSlug);
		String telegramUserId = "tg-" + UUID.randomUUID();

		HttpResponse<String> generateResponse = generateCode(tenantSlug, caller.id().toString());
		assertThat(generateResponse.statusCode()).isEqualTo(201);
		String code = extractCode(generateResponse.body());

		HttpResponse<String> confirmResponse = confirmLink(telegramUserId, code);
		assertThat(confirmResponse.statusCode()).isEqualTo(201);

		HttpResponse<String> lookupResponse = lookup(telegramUserId);
		assertThat(lookupResponse.statusCode()).isEqualTo(200);
		assertThat(lookupResponse.body()).contains("\"userId\":\"" + caller.id() + "\"");
		assertThat(lookupResponse.body()).contains("\"tenantId\":\"" + tenantSlug + "\"");
	}

	@Test
	void shouldReturn401WhenGeneratingWithoutCallerHeader() throws Exception {
		HttpResponse<String> response = generateCode(tenantSlug, null);

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/missing-caller-context\"");
	}

	@Test
	void shouldReturn401WhenGeneratingForCallerThatDoesNotExistInTenant() throws Exception {
		HttpResponse<String> response = generateCode(tenantSlug, UUID.randomUUID().toString());

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/caller-not-found\"");
	}

	@Test
	void shouldReturn404WhenConfirmingWithUnknownCode() throws Exception {
		HttpResponse<String> response = confirmLink("tg-unknown", "NOPE0000");

		assertThat(response.statusCode()).isEqualTo(404);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/telegram-link-code-not-found\"");
	}

	@Test
	void shouldReturn422WhenConfirmingWithExpiredCode() throws Exception {
		User caller = seedUser(tenantSlug);
		pendingTelegramLinkRepository.save(new PendingTelegramLink("EXPIRED1", tenantSlug, caller.id(),
				Instant.now().minusSeconds(1)));

		HttpResponse<String> response = confirmLink("tg-expired", "EXPIRED1");

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/telegram-link-code-expired\"");
		assertThat(pendingTelegramLinkRepository.findByCode("EXPIRED1")).isEmpty();
	}

	@Test
	void shouldLocalizeExpiredCodeErrorPerAcceptLanguage() throws Exception {
		User caller = seedUser(tenantSlug);
		pendingTelegramLinkRepository.save(new PendingTelegramLink("EXPIRED2", tenantSlug, caller.id(),
				Instant.now().minusSeconds(1)));

		HttpResponse<String> response = confirmLink("tg-expired-2", "EXPIRED2", "Accept-Language", "es");

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).contains("ha expirado");
	}

	@Test
	void shouldReturn404WhenLookingUpUnknownTelegramUserId() throws Exception {
		HttpResponse<String> response = lookup("tg-" + UUID.randomUUID());

		assertThat(response.statusCode()).isEqualTo(404);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/telegram-account-not-found\"");
	}

	@Test
	void shouldIsolateGeneratedCodesAcrossTenants() throws Exception {
		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		provisionTenantSchema.ensureSchemaExists(otherSlug);

		try {
			User callerA = seedUser(tenantSlug);
			User callerB = seedUser(otherSlug);

			String codeA = extractCode(generateCode(tenantSlug, callerA.id().toString()).body());
			String codeB = extractCode(generateCode(otherSlug, callerB.id().toString()).body());
			assertThat(codeA).isNotEqualTo(codeB);

			String telegramUserIdA = "tg-a-" + UUID.randomUUID();
			String telegramUserIdB = "tg-b-" + UUID.randomUUID();
			assertThat(confirmLink(telegramUserIdA, codeA).statusCode()).isEqualTo(201);
			assertThat(confirmLink(telegramUserIdB, codeB).statusCode()).isEqualTo(201);

			assertThat(lookup(telegramUserIdA).body()).contains("\"tenantId\":\"" + tenantSlug + "\"");
			assertThat(lookup(telegramUserIdB).body()).contains("\"tenantId\":\"" + otherSlug + "\"");
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + TenantSchemaName.fromSlug(otherSlug).value() + "\" CASCADE");
		}
	}

	@Test
	void shouldOverwritePreviousLinkWhenSameTelegramUserIdConfirmsInAnotherTenant() throws Exception {
		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		provisionTenantSchema.ensureSchemaExists(otherSlug);

		try {
			User callerA = seedUser(tenantSlug);
			User callerB = seedUser(otherSlug);
			String telegramUserId = "tg-shared-" + UUID.randomUUID();

			String codeA = extractCode(generateCode(tenantSlug, callerA.id().toString()).body());
			assertThat(confirmLink(telegramUserId, codeA).statusCode()).isEqualTo(201);
			assertThat(lookup(telegramUserId).body()).contains("\"tenantId\":\"" + tenantSlug + "\"");

			String codeB = extractCode(generateCode(otherSlug, callerB.id().toString()).body());
			assertThat(confirmLink(telegramUserId, codeB).statusCode()).isEqualTo(201);

			HttpResponse<String> finalLookup = lookup(telegramUserId);
			assertThat(finalLookup.body()).contains("\"tenantId\":\"" + otherSlug + "\"");
			assertThat(finalLookup.body()).contains("\"userId\":\"" + callerB.id() + "\"");
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + TenantSchemaName.fromSlug(otherSlug).value() + "\" CASCADE");
		}
	}
}
