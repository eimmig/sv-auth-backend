package com.stakevault.betting.auth.adapter.in.web;

import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.auth.domain.model.Role;

public record CreateUserResponse(UUID id, String name, String email, Role role, boolean mustChangePassword,
		Instant createdAt) {
}
