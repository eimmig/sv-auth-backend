package com.stakevault.betting.auth.adapter.in.web;

import java.util.List;
import java.util.UUID;

public record CreateTenantResponse(UUID userId, String email, String temporaryPassword,
		List<String> downstreamProvisioningFailures) {
}
