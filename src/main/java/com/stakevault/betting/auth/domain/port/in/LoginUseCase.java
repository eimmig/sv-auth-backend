package com.stakevault.betting.auth.domain.port.in;

import com.stakevault.betting.auth.domain.model.LoginResult;

public interface LoginUseCase {

	LoginResult login(String tenantSlug, String email, String rawPassword);
}
