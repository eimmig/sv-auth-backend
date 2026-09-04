package com.stakevault.betting.auth.domain.port.in;

import com.stakevault.betting.auth.domain.model.CreatedTenantAdmin;

public interface CreateTenantUseCase {

	CreatedTenantAdmin createTenant(String slug, String tenantName);
}
