package com.stakevault.betting.auth.domain.port.out;

public interface DownstreamTenantProvisioner {

	void provisionBetsService(String slug);

	void provisionStatsService(String slug);
}
