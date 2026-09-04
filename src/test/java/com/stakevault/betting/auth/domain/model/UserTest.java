package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void shouldAcceptValidData() {
		User user = new User(UUID.randomUUID(), "Ana", "ana@acme.com", "hash", Role.ADMIN, true, Instant.now());

		assertThat(user.name()).isEqualTo("Ana");
		assertThat(user.role()).isEqualTo(Role.ADMIN);
	}

	@Test
	void shouldRejectNullId() {
		assertThatThrownBy(() -> new User(null, "Ana", "ana@acme.com", "hash", Role.ADMIN, false, Instant.now()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectBlankEmail() {
		assertThatThrownBy(() -> new User(UUID.randomUUID(), "Ana", " ", "hash", Role.ADMIN, false, Instant.now()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullRole() {
		assertThatThrownBy(() -> new User(UUID.randomUUID(), "Ana", "ana@acme.com", "hash", null, false, Instant.now()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectBlankPasswordHash() {
		assertThatThrownBy(() -> new User(UUID.randomUUID(), "Ana", "ana@acme.com", "", Role.MEMBER, false, Instant.now()))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
