package com.stakevault.betting.auth.domain.port.out;

import java.util.UUID;

import com.stakevault.betting.auth.domain.model.Role;

public interface AccessTokenIssuer {

	String issue(UUID userId, String tenantSlug, Role role);
}
