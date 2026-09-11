package com.stakevault.betting.auth.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * @param downstreamProvisioningFailures nomes dos serviços (bets-service/stats-service) que
 * falharam ao provisionar o tenant - vazia quando os 2 deram certo (ou já estavam provisionados).
 * O tenant/admin deste serviço já foi criado com sucesso independente disso; o operador pode
 * repetir a chamada standalone do serviço que falhou (idempotente).
 */
public record CreatedTenantAdmin(UUID userId, String email, String temporaryPassword,
		List<String> downstreamProvisioningFailures) {
}
