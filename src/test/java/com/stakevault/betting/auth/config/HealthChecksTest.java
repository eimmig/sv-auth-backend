package com.stakevault.betting.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.stakevault.betting.auth.TestcontainersConfiguration;

/**
 * Prova que os dois endpoints de docs/OBSERVABILITY-AND-CONFIG.md respondem, e que a
 * prontidao inclui a checagem de conexao com o Postgres (indicador "db"), nao so o
 * ciclo de vida da aplicacao. HttpClient puro do JDK em vez de TestRestTemplate: esse
 * exige spring-boot-restclient no classpath (RestTemplateBuilder), dependencia extra
 * que este servico nao usa em nenhum outro lugar.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class HealthChecksTest {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	private HttpResponse<String> get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void liveness_respondeUp() throws Exception {
		HttpResponse<String> response = get("/actuator/health");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"status\":\"UP\"");
	}

	@Test
	void readiness_respondeUpEIncluiChecagemDoPostgres() throws Exception {
		HttpResponse<String> response = get("/actuator/health/readiness");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"status\":\"UP\"").contains("\"db\"");
	}
}
