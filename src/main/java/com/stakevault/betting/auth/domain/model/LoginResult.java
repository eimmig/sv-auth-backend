package com.stakevault.betting.auth.domain.model;

import java.util.UUID;

public record LoginResult(String token, boolean mustChangePassword, UUID userId, Role role) {
}
