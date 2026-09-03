package com.stakevault.betting.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Usuario dentro do schema de um tenant - sem tenantId, o schema ja e o tenant. */
public record User(
		UUID id,
		String name,
		String email,
		String passwordHash,
		Role role,
		boolean mustChangePassword,
		Instant createdAt) {

	public User {
		if (id == null || name == null || name.isBlank() || email == null || email.isBlank()
				|| passwordHash == null || passwordHash.isBlank() || role == null || createdAt == null) {
			throw new IllegalArgumentException("dados de usuario invalidos");
		}
	}
}
