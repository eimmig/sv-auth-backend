package com.stakevault.betting.auth.adapter.out.security;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.stakevault.betting.auth.domain.port.out.TelegramLinkCodeGenerator;

@Component
public class SecureRandomTelegramLinkCodeGenerator implements TelegramLinkCodeGenerator {

	private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int LENGTH = 8;

	private final SecureRandom random = new SecureRandom();

	@Override
	public String generate() {
		StringBuilder code = new StringBuilder(LENGTH);
		for (int i = 0; i < LENGTH; i++) {
			code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
		}
		return code.toString();
	}
}
