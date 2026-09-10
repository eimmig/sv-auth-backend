package com.stakevault.betting.auth.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextHolder;
import com.stakevault.betting.auth.domain.model.AdminRoleRequiredException;
import com.stakevault.betting.auth.domain.model.EmailAlreadyRegisteredException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.CreateUserUseCase;
import com.stakevault.betting.auth.domain.port.out.PasswordHasher;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

@Service
public class CreateUserService implements CreateUserUseCase {

	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;

	public CreateUserService(UserRepository userRepository, PasswordHasher passwordHasher) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
	}

	@Override
	public User createUser(UUID callerId, String name, String email, String rawPassword) {
		if (TenantContextHolder.current() == null) {
			throw new MissingTenantContextException();
		}

		boolean callerIsAdmin = userRepository.findById(callerId)
				.map(caller -> caller.role() == Role.ADMIN)
				.orElse(false);
		if (!callerIsAdmin) {
			throw new AdminRoleRequiredException();
		}

		User newUser = new User(UUID.randomUUID(), name, email, passwordHasher.hash(rawPassword), Role.MEMBER, false,
				Instant.now());
		try {
			return userRepository.save(newUser);
		} catch (DataIntegrityViolationException _) {
			throw new EmailAlreadyRegisteredException(email);
		}
	}
}
