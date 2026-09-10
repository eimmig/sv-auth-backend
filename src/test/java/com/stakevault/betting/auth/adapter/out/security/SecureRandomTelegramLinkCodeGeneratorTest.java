package com.stakevault.betting.auth.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SecureRandomTelegramLinkCodeGeneratorTest {

	private final SecureRandomTelegramLinkCodeGenerator generator = new SecureRandomTelegramLinkCodeGenerator();

	@Test
	void shouldGenerateCodeWithExpectedLength() {
		assertThat(generator.generate()).hasSize(8);
	}

	@Test
	void shouldGenerateOnlyUppercaseAlphanumericCharacters() {
		assertThat(generator.generate()).matches("^[A-Z0-9]{8}$");
	}

	@Test
	void shouldGenerateDistinctCodesAcrossCalls() {
		Set<String> codes = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			codes.add(generator.generate());
		}

		assertThat(codes).hasSize(100);
	}
}
