package com.stakevault.betting.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.stakevault.betting.auth.TestcontainersConfiguration;

/**
 * Prova que os dois endpoints de docs/OBSERVABILITY-AND-CONFIG.md respondem, e que a
 * prontidao inclui a checagem de conexao com o Postgres (indicador "db"), nao so o
 * ciclo de vida da aplicacao.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class HealthChecksTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void liveness_respondeUp() {
		var response = restTemplate.getForEntity("/actuator/health", String.class);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).contains("\"status\":\"UP\"");
	}

	@Test
	void readiness_respondeUpEIncluiChecagemDoPostgres() {
		var response = restTemplate.getForEntity("/actuator/health/readiness", String.class);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).contains("\"status\":\"UP\"").contains("\"db\"");
	}
}
