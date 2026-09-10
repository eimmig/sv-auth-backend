package com.stakevault.betting.auth.adapter.out.security;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.stakevault.betting.auth.domain.port.out.TemporaryPasswordGenerator;

@Component
public class SecureRandomPasswordGenerator implements TemporaryPasswordGenerator {

	private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
	private static final int LENGTH = 20;

	private final SecureRandom random = new SecureRandom();

	@Override
	public String generate() {
		StringBuilder password = new StringBuilder(LENGTH);
		for (int i = 0; i < LENGTH; i++) {
			password.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
		}
		return password.toString();
	}
}
