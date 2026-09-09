package com.stakevault.betting.auth.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextHolder;
import com.stakevault.betting.auth.domain.model.AdminRoleRequiredException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.ListUsersUseCase;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

@Service
public class ListUsersService implements ListUsersUseCase {

	private final UserRepository userRepository;

	public ListUsersService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public List<User> listUsers(UUID callerId) {
		if (TenantContextHolder.current() == null) {
			throw new MissingTenantContextException();
		}

		boolean callerIsAdmin = userRepository.findById(callerId)
				.map(caller -> caller.role() == Role.ADMIN)
				.orElse(false);
		if (!callerIsAdmin) {
			throw new AdminRoleRequiredException();
		}

		return userRepository.findAllOrderByName();
	}
}
