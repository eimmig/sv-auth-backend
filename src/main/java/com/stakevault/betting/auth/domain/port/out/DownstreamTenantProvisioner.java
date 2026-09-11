package com.stakevault.betting.auth.domain.port.out;

/**
 * Provisiona o schema do tenant nos demais serviços (bets-service/stats-service) depois que
 * auth-service já criou o schema e o admin localmente. Implementações tratam 409 (já
 * provisionado) como sucesso - idempotente por design nos dois serviços de destino.
 */
public interface DownstreamTenantProvisioner {

	void provisionBetsService(String slug);

	void provisionStatsService(String slug);
}
