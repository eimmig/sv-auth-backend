package com.stakevault.betting.auth.adapter.out.http;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.stakevault.betting.auth.domain.model.DownstreamProvisioningException;
import com.stakevault.betting.auth.domain.port.out.DownstreamTenantProvisioner;

@Component
public class RestClientDownstreamTenantProvisioner implements DownstreamTenantProvisioner {

	private static final Logger log = LoggerFactory.getLogger(RestClientDownstreamTenantProvisioner.class);

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
	private static final String ADMIN_API_KEY_HEADER = "X-Admin-Api-Key";

	private final RestClient betsServiceClient;
	private final RestClient statsServiceClient;
	private final String adminApiKey;
	private final String adminTenantsPath;

	public RestClientDownstreamTenantProvisioner(
			@Value("${tenant-provisioning.bets-service-url}") String betsServiceUrl,
			@Value("${tenant-provisioning.stats-service-url}") String statsServiceUrl,
			@Value("${admin.api-key}") String adminApiKey,
			@Value("${tenant-provisioning.admin-tenants-path:/api/v1/admin/tenants}") String adminTenantsPath) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.betsServiceClient = RestClient.builder().baseUrl(betsServiceUrl).requestFactory(requestFactory).build();
		this.statsServiceClient = RestClient.builder().baseUrl(statsServiceUrl).requestFactory(requestFactory).build();
		this.adminApiKey = adminApiKey;
		this.adminTenantsPath = adminTenantsPath;
	}

	@Override
	public void provisionBetsService(String slug) {
		provision(betsServiceClient, "bets-service", slug);
	}

	@Override
	public void provisionStatsService(String slug) {
		provision(statsServiceClient, "stats-service", slug);
	}

	private void provision(RestClient client, String serviceName, String slug) {
		try {
			client.post()
					.uri(adminTenantsPath)
					.header(ADMIN_API_KEY_HEADER, adminApiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("slug", slug))
					.retrieve()
					.toBodilessEntity();
		}
		catch (HttpClientErrorException.Conflict _) {
			log.debug("tenant {} already provisioned in {}", slug, serviceName);
		}
		catch (RestClientException e) {
			throw new DownstreamProvisioningException(serviceName, e);
		}
	}
}
