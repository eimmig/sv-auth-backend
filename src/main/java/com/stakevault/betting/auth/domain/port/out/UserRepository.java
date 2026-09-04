package com.stakevault.betting.auth.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.stakevault.betting.auth.domain.model.User;

public interface UserRepository {

	User save(User user);

	Optional<User> findById(UUID id);

	Optional<User> findByEmail(String email);
}
