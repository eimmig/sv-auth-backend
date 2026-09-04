package com.stakevault.betting.auth.adapter.out.security;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.stakevault.betting.auth.domain.port.out.PasswordHasher;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

	private static final int BCRYPT_MAX_BYTES = 72;

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Override
	public String hash(String rawPassword) {
		if (rawPassword.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
			throw new IllegalArgumentException("password exceeds " + BCRYPT_MAX_BYTES + " bytes, BCrypt cannot hash it safely");
		}
		return encoder.encode(rawPassword);
	}
}
