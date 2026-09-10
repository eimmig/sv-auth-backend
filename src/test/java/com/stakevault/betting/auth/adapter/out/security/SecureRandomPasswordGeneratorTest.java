package com.stakevault.betting.auth.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecureRandomPasswordGeneratorTest {

	private final SecureRandomPasswordGenerator generator = new SecureRandomPasswordGenerator();

	@Test
	void shouldGenerateTwentyCharacterPassword() {
		assertThat(generator.generate()).hasSize(20);
	}

	@Test
	void shouldOnlyUseUnambiguousAlphanumericCharacters() {
		assertThat(generator.generate()).matches("[A-HJ-NP-Za-hj-np-z2-9]+");
	}

	@Test
	void shouldGenerateDifferentPasswordsOnEachCall() {
		assertThat(generator.generate()).isNotEqualTo(generator.generate());
	}
}
