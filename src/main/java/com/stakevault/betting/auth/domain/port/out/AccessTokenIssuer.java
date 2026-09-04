package com.stakevault.betting.auth.domain.port.out;

import java.util.UUID;

public interface AccessTokenIssuer {

	String issue(UUID userId, String tenantSlug);
}
