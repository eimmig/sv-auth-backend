package com.stakevault.betting.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminTenantControllerIntegrationTest extends TenantSchemaIntegrationSupport {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	// Prova de verdade da orquestracao (auth-service chama bets-service/stats-service, ver
	// docs/DECISIONS-LOG.md item 3 - revertido pra orquestracao real a pedido do usuario,
	// 2026-09-11): 2 stubs HTTP reais respondendo a mesma rota administrativa que os servicos
	// de destino expoem de verdade, registrados via @DynamicPropertySource antes do contexto
	// Spring subir - nao mocka o adapter, exercita RestClientDownstreamTenantProvisioner real.
	private static final CopyOnWriteArrayList<String> BETS_SERVICE_CALLS = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<String> STATS_SERVICE_CALLS = new CopyOnWriteArrayList<>();
	// So o stub do stats-service simula queda (503) - o gatilho e o mesmo slug em ambas as
	// chamadas, entao o comportamento precisa ser por SERVIDOR, nao por conteudo do corpo
	// (achado real escrevendo este teste: os 2 stubs compartilhando o mesmo predicado de
	// gatilho faziam o bets-service tambem "cair" pro mesmo slug).
	private static final HttpServer BETS_SERVICE_STUB = startStub(BETS_SERVICE_CALLS, false);
	private static final HttpServer STATS_SERVICE_STUB = startStub(STATS_SERVICE_CALLS, true);

	@DynamicPropertySource
	static void registerDownstreamUrls(DynamicPropertyRegistry registry) {
		registry.add("tenant-provisioning.bets-service-url",
				() -> "http://localhost:" + BETS_SERVICE_STUB.getAddress().getPort());
		registry.add("tenant-provisioning.stats-service-url",
				() -> "http://localhost:" + STATS_SERVICE_STUB.getAddress().getPort());
	}

	private static HttpServer startStub(CopyOnWriteArrayList<String> receivedSlugs, boolean simulatesOutage) {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
			server.createContext("/api/v1/admin/tenants", exchange -> {
				String body = new String(exchange.getRequestBody().readAllBytes());
				receivedSlugs.add(body);
				// Slug "stats-outage" simula ESTE servidor fora do ar - so aplica no stub
				// configurado com simulatesOutage=true (ver comentario no campo STATS_SERVICE_STUB).
				int status = (simulatesOutage && body.contains("stats-outage")) ? 503 : 201;
				exchange.sendResponseHeaders(status, 0);
				exchange.getResponseBody().close();
			});
			server.start();
			return server;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@AfterAll
	static void stopDownstreamStubs() {
		BETS_SERVICE_STUB.stop(0);
		STATS_SERVICE_STUB.stop(0);
	}

	AdminTenantControllerIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
	}

	private HttpResponse<String> post(String body, String... headers) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/admin/tenants"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		for (int i = 0; i < headers.length; i += 2) {
			builder.header(headers[i], headers[i + 1]);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void shouldCreateTenantAndAdminUserOnValidRequest() throws Exception {
		String newSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		HttpResponse<String> response = post(
				"{\"slug\":\"" + newSlug + "\",\"tenantName\":\"Acme Corp\"}",
				"X-Admin-Api-Key", "test-admin-api-key");

		try {
			assertThat(response.statusCode()).isEqualTo(201);
			assertThat(response.body()).contains("\"email\":\"admin@" + newSlug + "\"");
			assertThat(response.body()).contains("\"temporaryPassword\"");
			assertThat(response.body()).contains("\"downstreamProvisioningFailures\":[]");
			boolean betsServiceCalled = BETS_SERVICE_CALLS.stream().anyMatch(body -> body.contains(newSlug));
			boolean statsServiceCalled = STATS_SERVICE_CALLS.stream().anyMatch(body -> body.contains(newSlug));
			assertThat(betsServiceCalled).isTrue();
			assertThat(statsServiceCalled).isTrue();
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"tenant_" + newSlug + "\" CASCADE");
		}
	}

	@Test
	void shouldStillCreateTenantLocallyAndReportFailureWhenStatsServiceIsDown() throws Exception {
		String newSlug = "test-stats-outage-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

		HttpResponse<String> response = post(
				"{\"slug\":\"" + newSlug + "\"}",
				"X-Admin-Api-Key", "test-admin-api-key");

		try {
			assertThat(response.statusCode()).isEqualTo(201);
			assertThat(response.body()).contains("\"downstreamProvisioningFailures\":[\"stats-service\"]");
			boolean betsServiceCalled = BETS_SERVICE_CALLS.stream().anyMatch(body -> body.contains(newSlug));
			assertThat(betsServiceCalled).isTrue();
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"tenant_" + newSlug + "\" CASCADE");
		}
	}

	@Test
	void shouldReturn409WhenSlugAlreadyProvisioned() throws Exception {
		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\"}",
				"X-Admin-Api-Key", "test-admin-api-key");

		assertThat(response.statusCode()).isEqualTo(409);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/tenant-already-provisioned\"");
		assertThat(response.body()).contains("\"instance\":\"/api/v1/admin/tenants\"");
	}

	@Test
	void shouldReturn422ForInvalidSlug() throws Exception {
		HttpResponse<String> response = post(
				"{\"slug\":\"1invalid\"}",
				"X-Admin-Api-Key", "test-admin-api-key");

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).contains("\"type\":\"https://docs/errors/invalid-tenant-slug\"");
	}

	@Test
	void shouldReturn422WithoutLeakingNullWhenSlugIsMissing() throws Exception {
		HttpResponse<String> response = post(
				"{\"tenantName\":\"Acme Corp\"}",
				"X-Admin-Api-Key", "test-admin-api-key");

		assertThat(response.statusCode()).isEqualTo(422);
		assertThat(response.body()).doesNotContain("null");
	}

	@Test
	void shouldReturn401WithoutCreatingSchemaWhenAdminApiKeyMissing() throws Exception {
		String newSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		HttpResponse<String> response = post("{\"slug\":\"" + newSlug + "\"}");

		assertThat(response.statusCode()).isEqualTo(401);
		Boolean schemaExists = jdbcTemplate.queryForObject(
				"SELECT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)",
				Boolean.class, "tenant_" + newSlug);
		assertThat(schemaExists).isFalse();
	}

	@Test
	void shouldLocalizeErrorTitleAndDetailPerAcceptLanguage() throws Exception {
		HttpResponse<String> response = post(
				"{\"slug\":\"" + tenantSlug + "\"}",
				"X-Admin-Api-Key", "test-admin-api-key",
				"Accept-Language", "es");

		assertThat(response.statusCode()).isEqualTo(409);
		assertThat(response.body()).contains("El tenant ya existe");
	}
}
