package com.stakevault.betting.auth.domain.port.out;

public interface DownstreamTenantProvisioner {

	void provisionBetsService(String slug);

	void provisionStatsService(String slug);

	/** Apaga os dados da organizacao no bets-service (epic-065); idempotente no servico. */
	void deleteFromBetsService(String slug);

	/** Apaga os dados da organizacao no stats-service (epic-065); idempotente no servico. */
	void deleteFromStatsService(String slug);
}
