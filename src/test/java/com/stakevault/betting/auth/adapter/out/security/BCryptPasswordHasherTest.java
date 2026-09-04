package com.stakevault.betting.auth.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptPasswordHasherTest {

	private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

	@Test
	void shouldHashToDifferentValueThanRawPassword() {
		String hash = hasher.hash("correct-horse-battery-staple");

		assertThat(hash).isNotEqualTo("correct-horse-battery-staple");
		assertThat(hash).startsWith("$2");
	}

	@Test
	void shouldProduceHashVerifiableByBCrypt() {
		String raw = "correct-horse-battery-staple";
		String hash = hasher.hash(raw);

		assertThat(new BCryptPasswordEncoder().matches(raw, hash)).isTrue();
	}

	@Test
	void shouldProduceDifferentHashesForSameInput() {
		String raw = "correct-horse-battery-staple";

		assertThat(hasher.hash(raw)).isNotEqualTo(hasher.hash(raw));
	}

	@Test
	void shouldRejectPasswordLongerThanSeventyTwoBytes() {
		String tooLong = "a".repeat(73);

		assertThatThrownBy(() -> hasher.hash(tooLong)).isInstanceOf(IllegalArgumentException.class);
	}
}
