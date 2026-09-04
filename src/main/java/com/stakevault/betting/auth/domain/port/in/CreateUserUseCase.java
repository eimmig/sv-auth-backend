package com.stakevault.betting.auth.domain.port.in;

import java.util.UUID;

import com.stakevault.betting.auth.domain.model.User;

public interface CreateUserUseCase {

	User createUser(UUID callerId, String name, String email, String rawPassword);
}
