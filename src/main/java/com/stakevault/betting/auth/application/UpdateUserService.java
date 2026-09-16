package com.stakevault.betting.auth.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextHolder;
import com.stakevault.betting.auth.domain.model.AdminRoleRequiredException;
import com.stakevault.betting.auth.domain.model.LastAdminCannotBeDemotedException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.model.UserNotFoundException;
import com.stakevault.betting.auth.domain.port.in.UpdateUserUseCase;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

@Service
public class UpdateUserService implements UpdateUserUseCase {

	private final UserRepository userRepository;

	public UpdateUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public User updateUser(UUID callerId, UUID targetUserId, String name, Role role) {
		if (TenantContextHolder.current() == null) {
			throw new MissingTenantContextException();
		}

		boolean callerIsAdmin = userRepository.findById(callerId)
				.map(caller -> caller.role() == Role.ADMIN)
				.orElse(false);
		if (!callerIsAdmin) {
			throw new AdminRoleRequiredException();
		}

		User target = userRepository.findById(targetUserId).orElseThrow(UserNotFoundException::new);

		if (target.role() == Role.ADMIN && role != Role.ADMIN && isLastAdmin(targetUserId)) {
			throw new LastAdminCannotBeDemotedException();
		}

		User updated = new User(target.id(), name, target.email(), target.passwordHash(), role,
				target.mustChangePassword(), target.createdAt());
		return userRepository.update(updated);
	}

	private boolean isLastAdmin(UUID targetUserId) {
		List<User> tenantUsers = userRepository.findAllOrderByName();
		return tenantUsers.stream()
				.filter(user -> user.role() == Role.ADMIN)
				.allMatch(admin -> admin.id().equals(targetUserId));
	}
}
