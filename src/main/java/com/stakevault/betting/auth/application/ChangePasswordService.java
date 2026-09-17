package com.stakevault.betting.auth.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextHolder;
import com.stakevault.betting.auth.domain.model.CallerNotFoundException;
import com.stakevault.betting.auth.domain.model.CurrentPasswordMismatchException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.ChangePasswordUseCase;
import com.stakevault.betting.auth.domain.port.out.PasswordHasher;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

@Service
public class ChangePasswordService implements ChangePasswordUseCase {

	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;

	public ChangePasswordService(UserRepository userRepository, PasswordHasher passwordHasher) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
	}

	@Override
	public void changePassword(UUID callerId, String currentPassword, String newPassword) {
		if (TenantContextHolder.current() == null) {
			throw new MissingTenantContextException();
		}

		User caller = userRepository.findById(callerId).orElseThrow(CallerNotFoundException::new);

		if (!passwordHasher.matches(currentPassword, caller.passwordHash())) {
			throw new CurrentPasswordMismatchException();
		}

		User updated = new User(caller.id(), caller.name(), caller.email(), passwordHasher.hash(newPassword),
				caller.role(), false, caller.createdAt());
		userRepository.update(updated);
	}
}
