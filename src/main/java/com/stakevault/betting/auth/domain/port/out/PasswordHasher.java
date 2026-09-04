package com.stakevault.betting.auth.domain.port.out;

public interface PasswordHasher {

	String hash(String rawPassword);
}
