package com.stakevault.betting.auth.domain.model;

import java.util.UUID;

public record CreatedTenantAdmin(UUID userId, String email, String temporaryPassword) {
}
