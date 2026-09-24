package com.stakevault.betting.auth.adapter.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.domain.model.DownstreamProvisioningException;

class RestClientDownstreamTenantProvisionerTest {

	private static final String ADMIN_KEY = "configured-admin-key";
	private static final String ADMIN_TENANTS_PATH = "/api/v1/admin/tenants";
	private static final List<String> RECEIVED_ADMIN_KEYS = new CopyOnWriteArrayList<>();
	private static final List<String> RECEIVED_BODIES = new CopyOnWriteArrayList<>();

	private static final HttpServer BETS_SERVICE_STUB = startStub();
	private static final HttpServer STATS_SERVICE_STUB = startStub();

	private final RestClientDownstreamTenantProvisioner provisioner = new RestClientDownstreamTenantProvisioner(
			"http://localhost:" + BETS_SERVICE_STUB.getAddress().getPort(),
			"http://localhost:" + STATS_SERVICE_STUB.getAddress().getPort(), ADMIN_KEY, ADMIN_TENANTS_PATH);

	private static HttpServer startStub() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
			server.createContext("/api/v1/admin/tenants", exchange -> {
				RECEIVED_ADMIN_KEYS.add(exchange.getRequestHeaders().getFirst("X-Admin-Api-Key"));
				RECEIVED_BODIES.add(readBody(exchange.getRequestBody()));
				int status = switch (bodySlug(RECEIVED_BODIES.getLast())) {
					case "already-provisioned" -> 409;
					case "unreachable-service-error" -> 500;
					default -> 201;
				};
				byte[] responseBody = new byte[0];
				exchange.sendResponseHeaders(status, responseBody.length);
				exchange.getResponseBody().write(responseBody);
				exchange.close();
			});
			server.start();
			return server;
		}
		catch (IOException e) {
			throw new UncheckedIOExceptionForTest(e);
		}
	}

	private static String readBody(InputStream input) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		input.transferTo(buffer);
		return buffer.toString(StandardCharsets.UTF_8);
	}

	private static final Pattern SLUG_PATTERN = Pattern.compile("\"slug\"\\s*:\\s*\"([^\"]+)\"");

	private static String bodySlug(String jsonBody) {
		Matcher matcher = SLUG_PATTERN.matcher(jsonBody);
		return matcher.find() ? matcher.group(1) : "";
	}

	@AfterAll
	static void stopStubs() {
		BETS_SERVICE_STUB.stop(0);
		STATS_SERVICE_STUB.stop(0);
	}

	@Test
	void shouldSendSlugAndAdminApiKeyToBetsService() {
		RECEIVED_ADMIN_KEYS.clear();
		RECEIVED_BODIES.clear();

		provisioner.provisionBetsService("acme");

		assertThat(RECEIVED_ADMIN_KEYS).containsExactly(ADMIN_KEY);
		assertThat(RECEIVED_BODIES.getFirst()).contains("\"slug\":\"acme\"");
	}

	@Test
	void shouldSendSlugAndAdminApiKeyToStatsService() {
		RECEIVED_ADMIN_KEYS.clear();
		RECEIVED_BODIES.clear();

		provisioner.provisionStatsService("acme");

		assertThat(RECEIVED_ADMIN_KEYS).containsExactly(ADMIN_KEY);
		assertThat(RECEIVED_BODIES.getFirst()).contains("\"slug\":\"acme\"");
	}

	@Test
	void shouldTreat409AlreadyProvisionedAsSuccessNotFailure() {
		assertThatCode(() -> provisioner.provisionBetsService("already-provisioned")).doesNotThrowAnyException();
	}

	@Test
	void shouldWrapUnexpectedStatusAsDownstreamProvisioningException() {
		assertThatThrownBy(() -> provisioner.provisionStatsService("unreachable-service-error"))
				.isInstanceOf(DownstreamProvisioningException.class)
				.satisfies(e -> assertThat(((DownstreamProvisioningException) e).serviceName())
						.isEqualTo("stats-service"));
	}

	@Test
	void shouldWrapConnectionFailureAsDownstreamProvisioningException() {
		var unreachableProvisioner = new RestClientDownstreamTenantProvisioner("http://localhost:1", "http://localhost:1",
				ADMIN_KEY, ADMIN_TENANTS_PATH);

		assertThatThrownBy(() -> unreachableProvisioner.provisionBetsService("acme"))
				.isInstanceOf(DownstreamProvisioningException.class)
				.satisfies(e -> assertThat(((DownstreamProvisioningException) e).serviceName())
						.isEqualTo("bets-service"));
	}

	private static final class UncheckedIOExceptionForTest extends RuntimeException {
		UncheckedIOExceptionForTest(IOException cause) {
			super(cause);
		}
	}
}
