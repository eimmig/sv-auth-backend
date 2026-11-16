package com.stakevault.betting.auth.adapter.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.domain.model.DownstreamProvisioningException;
import com.sun.net.httpserver.HttpServer;

class RestClientDownstreamTenantDeletionTest {

	private static final List<String> REQUESTS = new CopyOnWriteArrayList<>();
	private static final HttpServer BETS = stub(204);
	private static final HttpServer STATS = stub(500);

	private final RestClientDownstreamTenantProvisioner client = new RestClientDownstreamTenantProvisioner(
			"http://localhost:" + BETS.getAddress().getPort(), "http://localhost:" + STATS.getAddress().getPort(),
			"admin-key", "/api/v1/admin/tenants");

	private static HttpServer stub(int status) {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
			server.createContext("/api/v1/admin/tenants", exchange -> {
				REQUESTS.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath() + " "
						+ exchange.getRequestHeaders().getFirst("X-Admin-Api-Key"));
				exchange.sendResponseHeaders(status, -1);
				exchange.close();
			});
			server.start();
			return server;
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@AfterAll
	static void stopStubs() {
		BETS.stop(0);
		STATS.stop(0);
	}

	@Test
	void shouldSendTheDeleteWithTheAdminKeyToTheTenantPath() {
		client.deleteFromBetsService("acme");

		assertThat(REQUESTS).contains("DELETE /api/v1/admin/tenants/acme admin-key");
	}

	@Test
	void shouldReportWhichServiceFailed() {
		assertThatThrownBy(() -> client.deleteFromStatsService("acme"))
				.isInstanceOf(DownstreamProvisioningException.class)
				.satisfies(e -> assertThat(((DownstreamProvisioningException) e).serviceName()).isEqualTo("stats-service"));
	}
}
