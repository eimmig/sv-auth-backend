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
		String email = "ana@acme.com";
		Instant now = Instant.now();

		assertThatThrownBy(() -> new User(null, "Ana", email, "hash", Role.ADMIN, false, now))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectBlankEmail() {
		UUID id = UUID.randomUUID();
		Instant now = Instant.now();

		assertThatThrownBy(() -> new User(id, "Ana", " ", "hash", Role.ADMIN, false, now))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullRole() {
		UUID id = UUID.randomUUID();
		String email = "ana@acme.com";
		Instant now = Instant.now();

		assertThatThrownBy(() -> new User(id, "Ana", email, "hash", null, false, now))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectBlankPasswordHash() {
		UUID id = UUID.randomUUID();
		String email = "ana@acme.com";
		Instant now = Instant.now();

		assertThatThrownBy(() -> new User(id, "Ana", email, "", Role.MEMBER, false, now))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
